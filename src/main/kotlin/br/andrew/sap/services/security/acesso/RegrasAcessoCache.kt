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
 *  - No boot, carrega a versao MAIS NOVA. Se o SAP nao responde ou ela esta invalida, LANCA - o boot falha e o
 *    swarm mantem a task antiga no ar. Nao cai em silencio para o arquivo nem para uma versao anterior: isso
 *    poderia reabrir acessos revogados.
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

    /** Versao mais nova do SAP que este backend recusou (ilegivel ou invalida) e por isso NAO aplica; null se nenhuma. */
    fun versaoRejeitada(): Int? = ultimaRejeitada.takeIf { it > (ref.get()?.versao ?: 0) }

    fun carregarNoBoot(): Snapshot {
        val ultima = comRetentativa { repositorio.ultimaVersao() }
            ?: throw IllegalStateException(
                "Nao ha nenhuma versao das regras de acesso no SAP (ACESSO_REGRAS). " +
                    "Suba com acesso.regras.fonte=arquivo e fields.acesso=true para o seed criar a versao 1."
            )
        val completa = repositorio.buscar(ultima)
            ?: throw IllegalStateException("A versao $ultima das regras de acesso existe no indice mas nao pode ser lida no SAP")
        // So a versao MAIS RECENTE vale. Se ela estiver estragada o boot falha em vez de cair para uma anterior: a mais nova pode
        // ter revogado um acesso, e uma anterior o devolveria em silencio. O swarm mantem a task antiga no ar; para consertar,
        // suba com fonte=arquivo e restaure uma versao pela tela.
        val snapshot = montar(completa)
            ?: throw IllegalStateException(
                "A versao mais recente ($ultima) das regras de acesso e invalida e NAO sera trocada por uma anterior " +
                    "(ela poderia reabrir acessos revogados). Suba com acesso.regras.fonte=arquivo e fields.acesso=true, abra Regras de Acesso " +
                    "(a tela abre mesmo com a versao ilegivel) e use Restaurar numa versao anterior - isso grava uma versao nova valida."
            )
        ref.set(snapshot)
        log.info("Regras de acesso carregadas do SAP: versao {} ({} perfis)", snapshot.versao, snapshot.regras.size)
        return snapshot
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
}
