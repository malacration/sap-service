package br.andrew.sap.services.security.acesso

import br.andrew.sap.infrastructure.security.RulePathMatcher
import br.andrew.sap.model.acesso.AcessoRegrasVersao
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.security.RegrasArquivoService
import br.andrew.sap.services.security.interfaces.RuleService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

data class VersaoResumo(
    val versao: Int,
    val usuario: String?,
    val usuarioId: String?,
    val data: String?,
    val hora: String?,
    val origem: String?,
    val comentario: String?,
    val resumo: String?,
)

data class EstadoAtual(
    val versao: Int,
    val documento: RegrasDocumento,
    val resumo: VersaoResumo,
    /** "sap" ou "arquivo": qual implementacao esta de fato aplicando as regras no filtro. */
    val fonteAtiva: String,
    /** Versao que o cache desta instancia esta aplicando (so existe com fonte=sap). */
    val versaoEmVigor: Int?,
    val perfisProtegidos: List<String>,
    val arquivoLocal: String,
)

data class VersaoCompleta(val resumo: VersaoResumo, val documento: RegrasDocumento, val diff: List<MudancaPerfil>)

data class PreviaImportacao(
    val versaoAtual: Int,
    val modo: String,
    val diff: List<MudancaPerfil>,
    val erros: List<String>,
    val avisos: List<String>,
    val documento: RegrasDocumento,
    /** Há algo a gravar: mudança de acesso (diff) ou só de notas/ordem, que o diff de efeito não lista. */
    val temMudanca: Boolean = diff.isNotEmpty(),
)

data class RegraQueCasou(val perfil: String, val url: String, val actions: List<String>)

data class ResultadoSimulacao(
    val autorizado: Boolean,
    val regra: RegraQueCasou?,
    val perfisSemRegras: List<String>,
    val caminho: String,
)

data class EndpointConhecido(val url: String, val metodo: String)

/**
 * Regras do negocio da gestao das regras de acesso: ler, gravar versoes, importar/exportar,
 * restaurar e simular. Toda gravacao cria uma versao NOVA (baseVersao + 1) sobre a mais recente;
 * se a mais recente nao for a que a tela viu (`baseVersao`), e conflito. Gravacoes dentro desta
 * instancia sao serializadas por um lock; entre instancias quem serializa e o Code do SAP.
 */
@Service
@ConditionalOnProperty(value = ["fields.acesso"], havingValue = "true", matchIfMissing = false)
class RegrasAcessoService(
    private val repositorio: RegrasAcessoRepositorio,
    private val arquivo: RegrasArquivoService,
    private val cache: ObjectProvider<RegrasAcessoCache>,
    private val regraEmVigor: ObjectProvider<RuleService>,
    @Value("\${server.servlet.context-path:}") context: String,
) {
    private val log = LoggerFactory.getLogger(RegrasAcessoService::class.java)
    private val trava = ReentrantLock()
    private val matcher = RulePathMatcher(context)

    /** Trocavel nos testes. */
    internal var relogio: Clock = Clock.systemDefaultZone()

    // ------------------------------------------------------------------ leitura

    fun atual(): EstadoAtual {
        val (meta, documento) = ultima()
        return EstadoAtual(
            versao = meta.numeroDaVersao(),
            documento = documento,
            resumo = meta.paraResumo(),
            fonteAtiva = fonteAtiva(),
            versaoEmVigor = cache.getIfAvailable()?.versaoAtual(),
            perfisProtegidos = RegrasValidador.PERFIS_PROTEGIDOS.sorted(),
            arquivoLocal = arquivo.local(),
        )
    }

    fun versoes(): List<VersaoResumo> = repositorio.listar().map { it.paraResumo() }

    fun versao(numero: Int): VersaoCompleta {
        val meta = repositorio.buscar(numero) ?: throw RegrasNaoEncontradasException("A versão $numero das regras não existe.")
        val anterior = if (numero > 1) repositorio.buscar(numero - 1)?.let { documentoDe(it) } else null
        val documento = documentoDe(meta)
        return VersaoCompleta(meta.paraResumo(), documento, RegrasDiff.comparar(anterior, documento))
    }

    fun exportar(numero: Int?): String {
        val documento = if (numero == null) ultima().second else documentoDe(
            repositorio.buscar(numero) ?: throw RegrasNaoEncontradasException("A versão $numero das regras não existe.")
        )
        return RegrasYaml.exportar(documento)
    }

    /** O YAML do arquivo em uso no servidor, para comparar com o que esta cadastrado. */
    fun textoDoArquivo(): String = arquivo.textoBruto()

    // ------------------------------------------------------------------ gravacao

    fun salvarPerfil(quem: User, perfil: String, regras: List<RegraDoc>, baseVersao: Int, comentario: String?): EstadoAtual {
        RegrasValidador.erroDoNome(perfil)?.let { throw RegrasValidacaoException(listOf(it)) }
        gravarMudanca(quem, baseVersao, ORIGEM_TELA, comentario) { atual ->
            atual.copy(perfis = atual.perfis + (perfil to regras))
        }
        return atual()
    }

    fun removerPerfil(quem: User, perfil: String, baseVersao: Int, comentario: String?): EstadoAtual {
        if (perfil in RegrasValidador.PERFIS_PROTEGIDOS)
            throw RegrasValidacaoException(listOf("O perfil '$perfil' é usado pelo sistema e não pode ser excluído."))
        gravarMudanca(quem, baseVersao, ORIGEM_TELA, comentario) { atual ->
            if (perfil !in atual.perfis) throw RegrasNaoEncontradasException("O perfil '$perfil' não existe.")
            atual.copy(perfis = atual.perfis - perfil)
        }
        return atual()
    }

    fun restaurar(quem: User, numero: Int, baseVersao: Int, comentario: String?): EstadoAtual {
        val antiga = documentoDe(
            repositorio.buscar(numero) ?: throw RegrasNaoEncontradasException("A versão $numero das regras não existe.")
        )
        gravarMudanca(quem, baseVersao, ORIGEM_RESTAURACAO, comentario?.ifBlank { null } ?: "Restaurada a versão $numero") { antiga }
        return atual()
    }

    /**
     * Importa um YAML no formato do rules.yml. Com `simular=true` so devolve a previa (inclusive
     * quando o YAML e invalido - a previa traz os erros, sem HTTP de erro); sem simular, grava.
     */
    fun importar(quem: User, yaml: String, modo: String, simular: Boolean, baseVersao: Int?, comentario: String?): PreviaImportacao {
        val modoNormalizado = modo.trim().lowercase()
        if (modoNormalizado !in MODOS_IMPORTACAO)
            throw RegrasValidacaoException(listOf("Modo de importação inválido: '$modo'. Use substituir ou mesclar."))

        return trava.withLock {
            val (meta, atual) = ultima()
            val versaoAtual = meta.numeroDaVersao()

            val importado = try {
                RegrasYaml.importar(yaml)
            } catch (e: IllegalArgumentException) {
                if (simular) return@withLock PreviaImportacao(versaoAtual, modoNormalizado, emptyList(), listOf(e.message ?: "YAML inválido"), emptyList(), atual)
                throw RegrasValidacaoException(listOf(e.message ?: "YAML inválido"))
            }
            val proposto = if (modoNormalizado == MODO_MESCLAR) RegrasDiff.mesclar(atual, importado) else importado
            val validacao = RegrasValidador.validar(proposto, matcher)
            val diff = RegrasDiff.comparar(atual, validacao.documento)
            val erros = validacao.erros + erroDeProtegidos(atual, validacao.documento)
            val mudou = diff.isNotEmpty() || validacao.documento.perfis != atual.perfis
            val previa = PreviaImportacao(versaoAtual, modoNormalizado, diff, erros, validacao.avisos, validacao.documento, mudou)
            if (simular) return@withLock previa

            if (baseVersao == null) throw RegrasValidacaoException(listOf("Informe a versão em que a importação se baseia (baseVersao)."))
            if (baseVersao != versaoAtual) throw conflito(versaoAtual)
            if (erros.isNotEmpty()) throw RegrasValidacaoException(erros, validacao.avisos)
            if (!mudou) throw RegrasValidacaoException(listOf("O YAML não muda nada em relação à versão $versaoAtual."))
            gravarVersao(quem, versaoAtual + 1, validacao.documento, ORIGEM_IMPORTACAO, comentario, resumoDe(diff))
            previa
        }
    }

    /**
     * Cria a versao 1 a partir do arquivo em uso no servidor, se ainda nao ha nenhuma versao. Roda no
     * boot (AcessoRegrasSeeder). Duas instancias subindo juntas: a que perde a corrida recebe o
     * conflito do Code e segue.
     */
    fun semearSeVazio() {
        if (repositorio.ultimaVersao() != null) return
        val documento = try {
            RegrasYaml.importar(arquivo.textoBruto())
        } catch (e: Exception) {
            log.error("Seed das regras de acesso ignorado: nao foi possivel ler o arquivo {}: {}", arquivo.local(), e.message)
            return
        }
        val validacao = RegrasValidador.validar(documento, matcher)
        if (!validacao.ok) {
            log.error("Seed das regras de acesso ignorado: o arquivo {} tem erros: {}", arquivo.local(), validacao.erros)
            return
        }
        try {
            persistir("sistema", "0", 1, validacao.documento, ORIGEM_SEED, "Importado do arquivo ${arquivo.local()}", "seed: ${validacao.documento.perfis.size} perfis")
            log.info("Regras de acesso semeadas a partir de {} ({} perfis)", arquivo.local(), validacao.documento.perfis.size)
        } catch (e: RegrasConflitoException) {
            log.info("Seed das regras de acesso feito por outra instancia")
        }
    }

    // ------------------------------------------------------------------ simulacao e catalogo

    /**
     * Responde "este conjunto de perfis pode chamar este metodo neste caminho?" com o mesmo matcher do
     * filtro. `rascunho` testa as alteracoes ainda nao salvas. O caminho pode vir colado do console do
     * navegador (URL completa, com query): host, query e fragmento sao descartados.
     */
    fun simular(perfis: List<String>, metodo: String, caminho: String, rascunho: RegrasDocumento?): ResultadoSimulacao {
        val documento = if (rascunho != null) {
            val validacao = RegrasValidador.validar(rascunho, matcher)
            if (!validacao.ok) throw RegrasValidacaoException(validacao.erros, validacao.avisos)
            validacao.documento
        } else ultima().second

        val limpo = limparCaminho(caminho)
        val casou = perfis.asSequence()
            .flatMap { perfil -> documento.perfis[perfil].orEmpty().asSequence().map { perfil to it } }
            .firstOrNull { (_, regra) -> matcher.casa(regra.paraRule(), metodo, limpo) }
        return ResultadoSimulacao(
            autorizado = casou != null,
            regra = casou?.let { (perfil, regra) -> RegraQueCasou(perfil, regra.url, regra.actions) },
            perfisSemRegras = perfis.filter { documento.perfis[it].isNullOrEmpty() },
            caminho = limpo,
        )
    }

    /** Quais endpoints reais uma URL de regra alcanca (o padrao do endpoint troca {var} por asterisco). */
    fun cobertura(url: String, endpoints: List<EndpointConhecido>): List<EndpointConhecido> {
        val padrao = url.trim().replace(Regex("\\{[^/}]*}"), "*")
        if (matcher.problemaDoPadrao(padrao) != null) return emptyList()
        return endpoints.filter { matcher.matchPath(padrao, it.url) }
    }

    // ------------------------------------------------------------------ internos

    private fun gravarMudanca(
        quem: User,
        baseVersao: Int,
        origem: String,
        comentario: String?,
        transformar: (RegrasDocumento) -> RegrasDocumento,
    ) = trava.withLock {
        val (meta, atual) = ultima()
        val versaoAtual = meta.numeroDaVersao()
        if (versaoAtual != baseVersao) throw conflito(versaoAtual)

        val validacao = RegrasValidador.validar(transformar(atual), matcher)
        val erros = validacao.erros + erroDeProtegidos(atual, validacao.documento)
        if (erros.isNotEmpty()) throw RegrasValidacaoException(erros, validacao.avisos)
        val diff = RegrasDiff.comparar(atual, validacao.documento)
        // O diff de efeito ignora as notas; ainda assim mudar so uma nota (ou a ordem) e uma edicao a gravar.
        if (diff.isEmpty() && validacao.documento.perfis == atual.perfis)
            throw RegrasValidacaoException(listOf("Nada mudou em relação à versão $versaoAtual."))
        gravarVersao(quem, versaoAtual + 1, validacao.documento, origem, comentario, resumoDe(diff))
    }

    private fun resumoDe(diff: List<MudancaPerfil>): String =
        if (diff.isEmpty()) "sem mudança de acesso (só notas ou ordem)" else RegrasDiff.resumo(diff)

    /**
     * Perfis usados pelo codigo nao saem do cadastro por nenhum caminho. A exclusao ja recusava; sem
     * isto, importar em "substituir" ou restaurar uma versao antiga tiraria o perfil do mesmo jeito.
     */
    private fun erroDeProtegidos(atual: RegrasDocumento, proposto: RegrasDocumento): List<String> {
        val removidos = RegrasValidador.PERFIS_PROTEGIDOS
            .filter { it in atual.perfis && it !in proposto.perfis }.sorted()
        return if (removidos.isEmpty()) emptyList()
        else listOf("Isto removeria perfis usados pelo sistema, que não podem sair do cadastro: ${removidos.joinToString(", ")}.")
    }

    private fun gravarVersao(quem: User, numero: Int, documento: RegrasDocumento, origem: String, comentario: String?, resumo: String) {
        persistir(quem.userName.ifBlank { quem.id }, quem.id, numero, documento, origem, comentario, resumo)
        // Nesta instancia a mudanca vale na hora; as outras pegam no proximo refresh do cache.
        cache.getIfAvailable()?.aplicar(numero, documento)
    }

    /**
     * So grava no SAP. O seed usa este daqui (e nao o de cima) porque roda durante a criacao do proprio
     * cache quando o primeiro deploy ja sobe com fonte=sap - pedir o cache a esta altura seria uma
     * dependencia circular.
     */
    private fun persistir(
        usuario: String,
        usuarioId: String,
        numero: Int,
        documento: RegrasDocumento,
        origem: String,
        comentario: String?,
        resumo: String,
    ) {
        if (numero > VERSAO_MAXIMA)
            throw RegrasValidacaoException(listOf("O histórico chegou ao limite de $VERSAO_MAXIMA versões."))
        val agora = LocalDateTime.now(relogio)
        val codigo = AcessoRegrasVersao.codigo(numero)
        repositorio.gravar(
            AcessoRegrasVersao(
                Code = codigo,
                Name = codigo,
                U_Documento = RegrasCodec.toJson(documento),
                U_IdEscrita = UUID.randomUUID().toString(),
                U_Usuario = usuario.take(100),
                U_UsuarioId = usuarioId.take(50),
                U_Data = agora.toLocalDate().toString(),
                U_Hora = agora.format(HORA),
                U_Origem = origem,
                U_Comentario = comentario?.trim()?.take(254),
                U_Resumo = resumo.take(254),
            )
        )
    }

    private fun ultima(): Pair<AcessoRegrasVersao, RegrasDocumento> {
        val numero = repositorio.ultimaVersao()
            ?: throw RegrasNaoEncontradasException("Ainda não há nenhuma versão das regras cadastrada no SAP.")
        val meta = repositorio.buscar(numero)
            ?: throw RegrasNaoEncontradasException("A versão $numero das regras não foi encontrada.")
        return meta to documentoDe(meta)
    }

    private fun documentoDe(meta: AcessoRegrasVersao): RegrasDocumento =
        RegrasCodec.fromJson(meta.U_Documento ?: "{}")

    private fun fonteAtiva(): String = if (regraEmVigor.getObject() is SapRuleService) "sap" else "arquivo"

    private fun conflito(versaoAtual: Int) = RegrasConflitoException(
        "As regras foram alteradas por outra pessoa (agora estão na versão $versaoAtual). Recarregue a tela antes de salvar."
    )

    private fun limparCaminho(caminho: String): String {
        val semHost = caminho.trim().replace(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://[^/]+"), "")
        val semQuery = semHost.substringBefore('#').substringBefore('?')
        return if (semQuery.startsWith("/")) semQuery else "/$semQuery"
    }

    private fun AcessoRegrasVersao.paraResumo() = VersaoResumo(
        numeroDaVersao(), U_Usuario, U_UsuarioId, U_Data?.take(10), U_Hora, U_Origem, U_Comentario, U_Resumo,
    )

    companion object {
        const val ORIGEM_SEED = "SEED"
        const val ORIGEM_TELA = "TELA"
        const val ORIGEM_IMPORTACAO = "IMPORTACAO"
        const val ORIGEM_RESTAURACAO = "RESTAURACAO"
        /** O Code tem 6 digitos e o SAP ordena como texto: acima disso "1000000" ficaria antes de "999999". */
        const val VERSAO_MAXIMA = 999_999
        const val MODO_MESCLAR = "mesclar"
        private val MODOS_IMPORTACAO = setOf("substituir", MODO_MESCLAR)
        private val HORA = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
