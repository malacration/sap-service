package br.andrew.sap.services.security.acesso

import br.andrew.sap.model.acesso.AcessoRegrasVersao
import br.andrew.sap.services.security.Rule
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

/**
 * As regras em vigor, em memoria. E o que o filtro de autorizacao consulta a cada requisicao:
 * a requisicao nunca chama o SAP.
 *
 * Regras que este cache segue:
 *  - No boot, carrega a versao mais nova que le e valida; se a mais nova estiver estragada, desce
 *    para a anterior. Se o SAP nao responde ou nenhuma versao presta, LANCA - o boot falha e o swarm
 *    mantem a task antiga no ar. Nao cai em silencio para o arquivo: isso reabriria acessos revogados.
 *  - Nunca guarda um mapa vazio (o validador exige o `admin` com curinga).
 *  - So avanca: uma leitura lenta nao sobrescreve uma gravacao mais nova feita nesta instancia.
 *  - Se o SAP cair depois do boot, o ultimo snapshot valido continua valendo.
 */
class RegrasAcessoCache(
    private val repositorio: RegrasAcessoRepositorio,
    private val esperaBaseMs: Long = 500,
) {
    class Snapshot(val versao: Int, val regras: Map<String, List<Rule>>)

    private val log = LoggerFactory.getLogger(RegrasAcessoCache::class.java)
    private val ref = AtomicReference<Snapshot?>()
    @Volatile private var ultimaRejeitada: Int = 0

    fun atual(): Snapshot = ref.get() ?: throw IllegalStateException("As regras de acesso ainda nao foram carregadas")

    fun versaoAtual(): Int? = ref.get()?.versao

    fun carregarNoBoot(): Snapshot {
        val versoes = comRetentativa { repositorio.listar(VERSOES_NO_BOOT) }
        if (versoes.isEmpty())
            throw IllegalStateException(
                "Nao ha nenhuma versao das regras de acesso no SAP (ACESSO_REGRAS). " +
                    "Suba com acesso.regras.fonte=arquivo e fields.acesso=true para o seed criar a versao 1."
            )
        for (meta in versoes) {
            val completa = repositorio.buscar(meta.numeroDaVersao()) ?: continue
            val snapshot = montar(completa)
            if (snapshot != null) {
                ref.set(snapshot)
                if (meta !== versoes.first())
                    log.warn("As versoes mais novas das regras estavam invalidas; usando a versao {}", snapshot.versao)
                log.info("Regras de acesso carregadas do SAP: versao {} ({} perfis)", snapshot.versao, snapshot.regras.size)
                return snapshot
            }
        }
        throw IllegalStateException("Nenhuma das ${versoes.size} versoes mais recentes das regras de acesso tem um documento valido")
    }

    /** Troca o snapshot se a versao for maior que a atual. Devolve true se trocou. */
    fun aplicar(versao: Int, documento: RegrasDocumento): Boolean =
        trocar(Snapshot(versao, documento.paraRegras()))

    private fun trocar(novo: Snapshot): Boolean {
        while (true) {
            val atual = ref.get()
            if (atual != null && atual.versao >= novo.versao) return false
            if (ref.compareAndSet(atual, novo)) return true
        }
    }

    /** Refresh periodico: so baixa o documento se a versao no SAP for maior que a que esta em memoria. */
    fun conferir() {
        try {
            val noSap = repositorio.ultimaVersao() ?: return
            val emMemoria = ref.get()?.versao ?: 0
            if (noSap <= emMemoria || noSap == ultimaRejeitada) return
            val completa = repositorio.buscar(noSap) ?: return
            val snapshot = montar(completa)
            if (snapshot == null) {
                ultimaRejeitada = noSap
                return
            }
            if (trocar(snapshot))
                log.info("Regras de acesso atualizadas: versao {} ({} perfis)", snapshot.versao, snapshot.regras.size)
        } catch (t: Throwable) {
            // SAP fora do ar ou lento: segue com o ultimo snapshot valido e tenta de novo na proxima rodada.
            log.warn("Nao foi possivel conferir a versao das regras de acesso: {}", t.message)
        }
    }

    private fun montar(versao: AcessoRegrasVersao): Snapshot? {
        val numero = versao.numeroDaVersao()
        return try {
            val documento = RegrasCodec.fromJson(versao.U_Documento ?: "")
            val validacao = RegrasValidador.validar(documento)
            if (!validacao.ok) {
                log.error("A versao {} das regras de acesso e invalida e foi ignorada: {}", numero, validacao.erros)
                null
            } else {
                Snapshot(numero, validacao.documento.paraRegras())
            }
        } catch (e: Exception) {
            log.error("A versao {} das regras de acesso nao pode ser lida e foi ignorada: {}", numero, e.message)
            null
        }
    }

    // A tabela recem-criada as vezes ainda esta propagando no primeiro deploy e o SAP responde "is invalid".
    private fun <T> comRetentativa(bloco: () -> T): T {
        val tentativas = 5
        repeat(tentativas - 1) { tentativa ->
            try {
                return bloco()
            } catch (e: Exception) {
                if (e.message?.contains("is invalid") != true) throw e
                Thread.sleep(esperaBaseMs * (tentativa + 1))
            }
        }
        return bloco()
    }

    companion object {
        /** Quantas versoes o boot percorre ate achar uma valida. */
        const val VERSOES_NO_BOOT = 50
    }
}
