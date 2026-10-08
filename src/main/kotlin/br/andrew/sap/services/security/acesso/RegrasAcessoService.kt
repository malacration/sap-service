package br.andrew.sap.services.security.acesso

import br.andrew.sap.infrastructure.security.RulePathMatcher
import br.andrew.sap.infrastructure.security.keycloak.KeycloakAdminException
import br.andrew.sap.infrastructure.security.keycloak.KeycloakRolesGateway
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
    /** O Keycloak esta ligado neste backend (as roles vem de la). */
    val keycloakLigado: Boolean = false,
    /** Perfil novo vira role no Keycloak sozinho (keycloak.admin.enabled). Sem isso a role e criada la a mao. */
    val keycloakCriaRoles: Boolean = false,
    /** Versao do SAP que este backend recusou aplicar (ilegivel/invalida); ele segue com a anterior. Restaure uma versao valida. */
    val versaoRejeitada: Int? = null,
    /** Resultado da operacao que acabou de ser feita (ex.: a role nao pode ser criada no Keycloak). Vazio na leitura. */
    val avisos: List<String> = emptyList(),
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
    /** Avisos sobre as roles no Keycloak dos perfis novos (so preenchido ao gravar). */
    val avisosKeycloak: List<String> = emptyList(),
)

data class RegraQueCasou(val perfil: String, val url: String, val actions: List<String>)

data class ResultadoSimulacao(
    val autorizado: Boolean,
    val regra: RegraQueCasou?,
    val perfisSemRegras: List<String>,
    val caminho: String,
    /** Versao do cadastro usada na simulacao; null quando o teste foi feito com um rascunho. */
    val versaoSimulada: Int? = null,
    /** Essa versao ja e a que o filtro aplica neste backend? (false com fonte=arquivo ou enquanto o refresh nao chegou). null = rascunho. */
    val valendo: Boolean? = null,
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
    private val keycloak: ObjectProvider<KeycloakRolesGateway>,
    @Value("\${keycloak.enabled:false}") private val keycloakLigado: Boolean,
) {
    private val log = LoggerFactory.getLogger(RegrasAcessoService::class.java)
    private val trava = ReentrantLock()
    private val matcher = RulePathMatcher(context)

    /** Trocavel nos testes. */
    internal var relogio: Clock = Clock.systemDefaultZone()

    /**
     * Limita as TENTATIVAS ao Keycloak numa operacao: estourou, os perfis que faltam viram aviso, sem tentar. Nao
     * interrompe uma chamada ja em andamento - o timeout do cliente (keycloak.admin.timeout-ms) vale por chamada HTTP e um
     * perfil novo faz ate 3-4 em sequencia, entao o pior caso de um unico perfil ainda e da ordem de 15 a 20 s. Aceito:
     * e acao rara de admin, o SAP ja gravou e o aviso diz o que fazer.
     */
    internal var orcamentoKeycloakMs: Long = 12_000

    // ------------------------------------------------------------------ leitura

    fun atual(): EstadoAtual {
        val lida = lerUltima()
        val meta = lida.meta
        // Versao mais recente ilegivel: a tela ainda abre (com o documento vazio) para o operador poder restaurar uma anterior.
        val aviso = if (lida.documento == null) listOf(
            "A versão ${meta.numeroDaVersao()} das regras está ilegível ou inválida e não é aplicada. Use Restaurar numa versão anterior (aba Histórico)."
        ) else emptyList()
        return EstadoAtual(
            versao = meta.numeroDaVersao(),
            documento = lida.documento ?: RegrasDocumento(),
            resumo = meta.paraResumo(),
            avisos = aviso,
            versaoRejeitada = cache.getIfAvailable()?.versaoRejeitada(),
            fonteAtiva = fonteAtiva(),
            versaoEmVigor = cache.getIfAvailable()?.versaoAtual(),
            perfisProtegidos = RegrasValidador.PERFIS_PROTEGIDOS.sorted(),
            arquivoLocal = arquivo.local(),
            keycloakLigado = keycloakLigado,
            keycloakCriaRoles = keycloak.getIfAvailable() != null,
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
        val (antes, depois, numero) = gravarMudanca(quem, baseVersao, ORIGEM_TELA, comentario) { atual ->
            atual.copy(perfis = atual.perfis + (perfil to regras))
        }
        return estadoDepoisDeGravar(numero, depois, roleNoKeycloak(antes, depois))
    }

    fun removerPerfil(quem: User, perfil: String, baseVersao: Int, comentario: String?): EstadoAtual {
        if (perfil in RegrasValidador.PERFIS_PROTEGIDOS)
            throw RegrasValidacaoException(listOf("O perfil '$perfil' é usado pelo sistema e não pode ser excluído."))
        val (_, depois, numero) = gravarMudanca(quem, baseVersao, ORIGEM_TELA, comentario) { atual ->
            if (perfil !in atual.perfis) throw RegrasNaoEncontradasException("O perfil '$perfil' não existe.")
            atual.copy(perfis = atual.perfis - perfil)
        }
        // Nunca apaga a role no Keycloak sozinho: tiraria o perfil de todo usuario que o tem, e o realm e do grupo todo.
        val aviso = if (keycloakLigado)
            listOf("O perfil '$perfil' saiu do cadastro, mas a role '$perfil' continua no Keycloak. Remova lá se ninguém mais a usa.")
        else emptyList()
        return estadoDepoisDeGravar(numero, depois, aviso)
    }

    fun restaurar(quem: User, numero: Int, baseVersao: Int, comentario: String?): EstadoAtual {
        val antiga = documentoDe(
            repositorio.buscar(numero) ?: throw RegrasNaoEncontradasException("A versão $numero das regras não existe.")
        )
        val (antes, depois, nova) = gravarMudanca(quem, baseVersao, ORIGEM_RESTAURACAO, comentario?.ifBlank { null } ?: "Restaurada a versão $numero") { antiga }
        return estadoDepoisDeGravar(nova, depois, roleNoKeycloak(antes, depois))
    }

    /**
     * Importa um YAML no formato do rules.yml. Com `simular=true` so devolve a previa (inclusive
     * quando o YAML e invalido - a previa traz os erros, sem HTTP de erro); sem simular, grava.
     */
    fun importar(quem: User, yaml: String, modo: String, simular: Boolean, baseVersao: Int?, comentario: String?): PreviaImportacao {
        val modoNormalizado = modo.trim().lowercase()
        if (modoNormalizado !in MODOS_IMPORTACAO)
            throw RegrasValidacaoException(listOf("Modo de importação inválido: '$modo'. Use substituir ou mesclar."))

        // O Keycloak so e chamado DEPOIS de soltar a trava: uma chamada lenta nao pode segurar outras gravacoes.
        val (previa, antes) = trava.withLock {
            val (meta, atual) = ultima()
            val versaoAtual = meta.numeroDaVersao()

            val importado = try {
                RegrasYaml.importar(yaml)
            } catch (e: IllegalArgumentException) {
                if (simular) return@withLock PreviaImportacao(versaoAtual, modoNormalizado, emptyList(), listOf(e.message ?: "YAML inválido"), emptyList(), atual) to null
                throw RegrasValidacaoException(listOf(e.message ?: "YAML inválido"))
            }
            val proposto = if (modoNormalizado == MODO_MESCLAR) RegrasDiff.mesclar(atual, importado) else importado
            val validacao = RegrasValidador.validar(proposto, matcher)
            val diff = RegrasDiff.comparar(atual, validacao.documento)
            val erros = validacao.erros + erroDeProtegidos(atual, validacao.documento)
            val mudou = diff.isNotEmpty() || validacao.documento.perfis != atual.perfis
            val previa = PreviaImportacao(versaoAtual, modoNormalizado, diff, erros, validacao.avisos, validacao.documento, mudou)
            if (simular) return@withLock previa to null

            if (baseVersao == null) throw RegrasValidacaoException(listOf("Informe a versão em que a importação se baseia (baseVersao)."))
            if (baseVersao != versaoAtual) throw conflito(versaoAtual)
            if (erros.isNotEmpty()) throw RegrasValidacaoException(erros, validacao.avisos)
            if (!mudou) throw RegrasValidacaoException(listOf("O YAML não muda nada em relação à versão $versaoAtual."))
            gravarVersao(quem, versaoAtual + 1, validacao.documento, ORIGEM_IMPORTACAO, comentario, resumoDe(diff))
            previa to atual
        }
        return if (antes == null) previa else previa.copy(avisosKeycloak = roleNoKeycloak(antes, previa.documento))
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
        var versao: Int? = null
        val documento = if (rascunho != null) {
            val validacao = RegrasValidador.validar(rascunho, matcher)
            if (!validacao.ok) throw RegrasValidacaoException(validacao.erros, validacao.avisos)
            validacao.documento
        } else {
            val (meta, doc) = ultima()
            versao = meta.numeroDaVersao()
            doc
        }

        val limpo = limparCaminho(caminho)
        val casou = perfis.asSequence()
            .flatMap { perfil -> documento.perfis[perfil].orEmpty().asSequence().map { perfil to it } }
            .firstOrNull { (_, regra) -> matcher.casa(regra.paraRule(), metodo, limpo) }
        return ResultadoSimulacao(
            autorizado = casou != null,
            regra = casou?.let { (perfil, regra) -> RegraQueCasou(perfil, regra.url, regra.actions) },
            perfisSemRegras = perfis.filter { documento.perfis[it].isNullOrEmpty() },
            caminho = limpo,
            versaoSimulada = versao,
            valendo = versao?.let { fonteAtiva() == "sap" && cache.getIfAvailable()?.versaoAtual() == it },
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
    ): Triple<RegrasDocumento, RegrasDocumento, Int> = trava.withLock {
        val lida = lerUltima()
        val versaoAtual = lida.meta.numeroDaVersao()
        if (versaoAtual != baseVersao) throw conflito(versaoAtual)
        // Restaurar e o unico caminho que pode partir de uma ultima versao ilegivel: e como o operador se recupera.
        val legivel = lida.documento != null
        val atual = lida.documento
            ?: if (origem == ORIGEM_RESTAURACAO) RegrasDocumento()
            else throw RegrasValidacaoException(listOf("A versão $versaoAtual das regras está ilegível ou inválida. Restaure uma versão anterior (aba Histórico) antes de editar."))
        // Base ilegivel: os perfis protegidos que existiam antes ainda nao podem sair; compara com a ultima versao LEGIVEL.
        val baseParaProtegidos = if (legivel) atual else ultimaLegivelAntesDe(versaoAtual)

        val validacao = RegrasValidador.validar(transformar(atual), matcher)
        val erros = validacao.erros + erroDeProtegidos(baseParaProtegidos, validacao.documento)
        if (erros.isNotEmpty()) throw RegrasValidacaoException(erros, validacao.avisos)
        val diff = RegrasDiff.comparar(atual, validacao.documento)
        // O diff de efeito ignora as notas; ainda assim mudar so uma nota (ou a ordem) e uma edicao a gravar.
        if (diff.isEmpty() && validacao.documento.perfis == atual.perfis)
            throw RegrasValidacaoException(listOf("Nada mudou em relação à versão $versaoAtual."))
        gravarVersao(quem, versaoAtual + 1, validacao.documento, origem, comentario, resumoDe(diff))
        // Sem base legivel nao ha "perfis novos" a provisionar no Keycloak (seria tudo): antes = depois.
        Triple(if (legivel) atual else validacao.documento, validacao.documento, versaoAtual + 1)
    }

    /**
     * A versao legivel e valida mais recente anterior a `numero`, para comparar uma restauracao quando a ultima esta estragada.
     * Sem versao anterior nenhuma (so existe a estragada) a base e vazia. Havendo anteriores mas nenhuma legivel ao alcance da
     * busca, NAO assume vazio - isso deixaria remover um perfil protegido - e recusa: precisa ser consertado no SAP.
     */
    private fun ultimaLegivelAntesDe(numero: Int): RegrasDocumento {
        val anteriores = repositorio.listar(LIMITE_BUSCA_DE_BASE).map { it.numeroDaVersao() }.filter { it < numero }.sortedDescending()
        for (n in anteriores) {
            val valido = repositorio.buscar(n)?.let { lerValido(it) }
            if (valido != null) return valido
        }
        if (anteriores.isEmpty() && numero <= LIMITE_BUSCA_DE_BASE) return RegrasDocumento()
        throw RegrasValidacaoException(
            listOf(
                "Não foi possível achar uma versão anterior legível para conferir os perfis protegidos antes de restaurar " +
                    "(as últimas $LIMITE_BUSCA_DE_BASE versões estão ilegíveis ou inválidas). Corrija uma versão direto no SAP (ACESSO_REGRAS)."
            )
        )
    }

    /**
     * Resposta depois de gravar. Se reler o SAP falhar logo apos o POST, a gravacao NAO se perde nem o aviso do
     * Keycloak: responde com o que acabou de ser gravado e manda recarregar a tela.
     */
    private fun estadoDepoisDeGravar(numero: Int, documento: RegrasDocumento, avisos: List<String>): EstadoAtual =
        try {
            // Os avisos de atual() (ex.: a versao mais recente ficou ilegivel) nao podem se perder atras dos da gravacao.
            atual().let { it.copy(avisos = it.avisos + avisos) }
        } catch (e: Exception) {
            log.warn("Versao {} das regras gravada, mas nao foi possivel reler o estado: {}", numero, e.message)
            EstadoAtual(
                versao = numero,
                documento = documento,
                resumo = VersaoResumo(numero, null, null, null, null, null, null, null),
                fonteAtiva = fonteAtiva(),
                versaoEmVigor = cache.getIfAvailable()?.versaoAtual(),
                perfisProtegidos = RegrasValidador.PERFIS_PROTEGIDOS.sorted(),
                arquivoLocal = arquivo.local(),
                keycloakLigado = keycloakLigado,
                keycloakCriaRoles = keycloak.getIfAvailable() != null,
                avisos = avisos + "A gravação foi concluída (versão $numero), mas não foi possível reler o histórico. Recarregue a tela.",
            )
        }

    /**
     * Perfil novo no cadastro = role nova no Keycloak. O SAP ja foi gravado: uma falha aqui NAO desfaz o perfil,
     * so vira aviso com o nome exato da role para criar a mao. Sem a integracao ligada, o aviso so aparece se o
     * Keycloak esta em uso (do contrario nao ha onde criar a role).
     */
    private fun roleNoKeycloak(antes: RegrasDocumento, depois: RegrasDocumento): List<String> {
        val novos = depois.perfis.keys.filter { it !in antes.perfis }.sorted()
        if (novos.isEmpty() || !keycloakLigado) return emptyList()
        val gateway = keycloak.getIfAvailable()
            ?: return novos.map { "Crie no Keycloak a role '$it' (client de login) com esse nome exato para atribuí-la a usuários." }
        val avisos = mutableListOf<String>()
        var motivoDaFalha: String? = null
        val inicio = System.nanoTime()
        fun estourou() = (System.nanoTime() - inicio) / 1_000_000 > orcamentoKeycloakMs
        for (perfil in novos) {
            if (motivoDaFalha == null && estourou()) motivoDaFalha = "tempo esgotado falando com o Keycloak"
            // Depois da primeira falha nao insiste nos demais (Keycloak fora do ar ou sem permissao): cada tentativa custaria um timeout.
            if (motivoDaFalha != null) {
                avisos.add(avisoDeFalha(perfil, motivoDaFalha))
                continue
            }
            try {
                if (gateway.roleDeRealmExiste(perfil) == true)
                    avisos.add(
                        "Atenção: já existe no Keycloak uma role de REALM chamada '$perfil'. O login aceita roles de realm e de client " +
                            "com o mesmo nome, então quem já tem essa role de realm passa a receber este perfil sem nenhuma atribuição nova."
                    )
                if (estourou()) throw KeycloakAdminException("tempo esgotado falando com o Keycloak")
                if (!gateway.garantirRole(perfil))
                    avisos.add(
                        "A role '$perfil' já existia no Keycloak (não foi recriada). Quem já a tem passa a receber as regras deste perfil; " +
                            "confira quem são essas pessoas."
                    )
            } catch (e: Exception) {
                // So a mensagem das nossas excecoes (controlada) chega a tela; qualquer outra fica no log.
                motivoDaFalha = if (e is KeycloakAdminException) (e.message ?: "erro no Keycloak") else "erro inesperado, veja o log do servidor"
                log.warn("Perfil '{}' salvo, mas a role nao foi criada no Keycloak: {}", perfil, motivoDaFalha, e)
                avisos.add(avisoDeFalha(perfil, motivoDaFalha))
            }
        }
        return avisos
    }

    private fun avisoDeFalha(perfil: String, motivo: String) =
        "O perfil '$perfil' foi salvo, mas não foi possível criar a role no Keycloak ($motivo). Crie lá a role '$perfil'."

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

    private class UltimaLida(val meta: AcessoRegrasVersao, val documento: RegrasDocumento?)

    /** A versao mais recente; `documento` e null se o texto gravado no SAP nao pode ser interpretado. */
    private fun lerUltima(): UltimaLida {
        val numero = repositorio.ultimaVersao()
            ?: throw RegrasNaoEncontradasException("Ainda não há nenhuma versão das regras cadastrada no SAP.")
        val meta = repositorio.buscar(numero)
            ?: throw RegrasNaoEncontradasException("A versão $numero das regras não foi encontrada.")
        val documento = lerValido(meta)
        return UltimaLida(meta, documento)
    }

    /** O documento da versao, ou null se nao da para interpretar OU se nao passa na validacao (o filtro nunca o aplicaria). */
    private fun lerValido(meta: AcessoRegrasVersao): RegrasDocumento? {
        val numero = meta.numeroDaVersao()
        return try {
            val documento = documentoDe(meta)
            val validacao = RegrasValidador.validar(documento, matcher)
            if (validacao.ok) documento else {
                log.error("A versao {} das regras de acesso e invalida no SAP: {}", numero, validacao.erros)
                null
            }
        } catch (e: Exception) {
            log.error("A versao {} das regras de acesso esta ilegivel no SAP: {}", numero, e.message)
            null
        }
    }

    private fun ultima(): Pair<AcessoRegrasVersao, RegrasDocumento> {
        val lida = lerUltima()
        val documento = lida.documento ?: throw RegrasValidacaoException(
            listOf("A versão ${lida.meta.numeroDaVersao()} das regras está ilegível ou inválida. Restaure uma versão anterior (aba Histórico) antes de editar.")
        )
        return lida.meta to documento
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
        /** Quantas versoes (a partir da mais recente) a busca de uma base legivel para restaurar examina. */
        private const val LIMITE_BUSCA_DE_BASE = 50
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
