package br.andrew.sap.schedules

import JournalEntry
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.sap.documents.base.DistribuicaoCustoByBranch
import br.andrew.sap.model.sap.journal.OriginalJournal
import br.andrew.sap.model.sap.sistema.SapUser
import br.andrew.sap.services.cadastro.ProfitCenterService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.journal.JournalMemoHandle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@ConditionalOnProperty(value = ["lc.centrodecusto.grupoeconomico.enable"], havingValue = "true", matchIfMissing = false)
class AtualizarCentrodeCustoLancamentoSchedule(
    val journalEntriesService: JournalEntriesService,
    val journalMemoHandle: JournalMemoHandle,
    @Value("\${lc.centrodecusto.dias:5}") private val dias: Long,
    @Value("\${lc.centrodecusto.contas:4.9.1.001.00001,4.2.1.001.00007}") private val contas: List<String>,
    @Value("\${lc.centrodecusto.filial:2}") private val filial: Int,
    @Value("\${lc.centrodecusto.grupo-economico:500}") private val grupoEconomico: String,
    @Value("\${lc.centrodecusto.centro-custo:50000205}") private val centroCusto: String,
    @Value("\${lc.centrodecusto.padrao-por-filial:}") private val padraoPorFilialConfig: List<String>,
    val profitCenterService: ProfitCenterService,
    val currentSapUser: SapUser
) {

    //filial:grupoEconomico:centroCusto - entrada com campo em branco e descartada, senao gravaria
    //dimensao vazia que o JsonInclude NON_EMPTY omite do PATCH sem ninguem perceber
    private val padraoPorFilial: Map<String, DistribuicaoCustoByBranch> = padraoPorFilialConfig
        .map { it.trim().split(":").map { campo -> campo.trim() } }
        .filter { campos -> campos.size == 3 && campos.none { it.isBlank() } }
        .associate { (filial, grupo, centro) -> filial to DistribuicaoCustoByBranch(filial, grupo, centro) }

    private val logger: Logger = LoggerFactory.getLogger(AtualizarCentrodeCustoLancamentoSchedule::class.java)

    companion object {
        private val ORIGENS_PAGAMENTO = listOf(
            OriginalJournal.ttVendorPayment.toString(),
            OriginalJournal.ttReceipt.toString()
        )
        private val ORIGENS_TRATADAS = ORIGENS_PAGAMENTO + listOf(
            OriginalJournal.ttJournalEntry.toString(),
            OriginalJournal.ttProductionOrder.toString(),
            OriginalJournal.ttGeneralReleaseFromStock.toString()
        )
    }

    @Scheduled(fixedDelay = 60000)
    fun atualizarLancamentosContabeis() {
        val dataLimite = LocalDate.now().minusDays(dias).toString()
        try {
            val lancamentos = journalEntriesService.get(
                Filter(
                    Predicate("U_Atualizar_Centro_de_Custo", 0, Condicao.EQUAL),
                    Predicate("OriginalJournal", ORIGENS_TRATADAS, Condicao.IN),
                    Predicate("ReferenceDate", dataLimite, Condicao.GREAT_EQUAL)
                ),
                OrderBy("ReferenceDate", Order.DESC)
            ).tryGetPageValues<JournalEntry>(Pageable.unpaged())

            logger.info("Inicnando job de atualizar centro de custo")

            //falha ao carregar os inativos nao pode abortar o job: sem eles o comportamento e o
            //de antes, nenhum rateio e substituido. Sem padrao configurado a substituicao seria
            //no-op, entao nem vale a consulta
            val inativos = if(padraoPorFilial.isEmpty()) listOf() else try {
                profitCenterService.inativos()
            } catch (ex: Exception) {
                logger.error("Erro ao carregar as regras de reparticao inativas", ex)
                listOf()
            }
            val inativosGrupo = inativos.filter { it.isGrupoEconomico() }.map { it.CenterCode }.toSet()
            val inativosCentro = inativos.filter { it.isCentroCusto() }.map { it.CenterCode }.toSet()

            lancamentos.forEach { lancamento ->
                try {
                    lancamento.U_Atualizar_Centro_de_Custo = 1;
                    if(lancamento.JdtNum == null)
                        throw Exception("Lançamento contábil com JdtNum nulo. Ignorando processamento.")

                    val ehPagamento = lancamento.OriginalJournal in ORIGENS_PAGAMENTO

                    val lcComCentro : JournalEntry = if(ehPagamento && lancamento.hasContaResultado()){
                        logger.info("Atualizando centro de custo do lançamento contábil. JdtNum[${lancamento.JdtNum}]")
                        journalMemoHandle.atribuiCentroCustoEmContasRecebeOuPagar(lancamento)
                    } else lancamento

                    val trocas = if(ehPagamento)
                        lcComCentro.substituiRateioInativo(inativosGrupo,inativosCentro,padraoPorFilial)
                    else listOf()
                    if(trocas.isNotEmpty())
                        logger.info("Rateio inativo substituido pelo padrao da filial. JdtNum[${lcComCentro.JdtNum}]: ${trocas.joinToString("; ")}")

                    if(lcComCentro.preencheCentroCustoDasContas(contas,filial,grupoEconomico,centroCusto))
                        logger.info("Centro de custo padrao aplicado nas contas $contas. JdtNum[${lcComCentro.JdtNum}]")

                    journalEntriesService.update(lcComCentro,lcComCentro.JdtNum.toString())
                } catch (ex: Exception) {
                    logger.error("Erro ao atualizar o lançamento contábil ${lancamento.JdtNum}", ex)
                    //marcar como processado tambem falha quando o lancamento tem conta que exige
                    //regra de reparticao sem preencher: sem esse try a excecao aborta o forEach
                    //inteiro e o job trava no mesmo registro a cada rodada
                    try {
                        journalEntriesService.update(lancamento,lancamento.JdtNum.toString())
                    } catch (marcacao: Exception) {
                        //mesma causa do erro acima e o lancamento volta a cada rodada: sem stack
                        //para nao encher o log de stack repetida a cada 60s
                        logger.warn("Lançamento ${lancamento.JdtNum} continua pendente: ${marcacao.message}")
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Erro ao buscar e processar lançamentos contábeis", ex)
        }
        logger.info("Finalizando job de atualizar centro de custo")
    }
}
