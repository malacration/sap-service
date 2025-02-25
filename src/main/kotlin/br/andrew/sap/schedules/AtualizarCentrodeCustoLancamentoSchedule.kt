package br.andrew.sap.schedules

import JournalEntry
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.sap.SapUser
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
    @Value("\${lc.memo.dias:5}") private val dias: Long,
    val currentSapUser: SapUser
) {

    private val logger: Logger = LoggerFactory.getLogger(AtualizarCentrodeCustoLancamentoSchedule::class.java)

    @Scheduled(fixedDelay = 60000)
    fun atualizarLancamentosContabeis() {
        val dataLimite = LocalDate.now().minusDays(dias).toString()
        try {
            val lancamentos = journalEntriesService.get(
                Filter(
                    Predicate("U_Atualizar_Centro_de_Custo", 0, Condicao.EQUAL),
                    Predicate("OriginalJournal", listOf("ttVendorPayment", "ttReceipt"), Condicao.IN),
                    Predicate("ReferenceDate", dataLimite, Condicao.GREAT_EQUAL)
                ),
                OrderBy("ReferenceDate", Order.DESC)
            ).tryGetPageValues<JournalEntry>(Pageable.unpaged())


            lancamentos.filter{
                it.hasContaResultado()
            }.forEach { lancamento ->
                lancamento.U_Atualizar_Centro_de_Custo = 1
                try {
                    if(lancamento.JdtNum == null)
                        throw Exception("Lançamento contábil com JdtNum nulo. Ignorando processamento.")
                    logger.info("Atualizando centro de custo do lançamento contábil. JdtNum[${lancamento.JdtNum}]")
                    val lcComCentro : JournalEntry = journalMemoHandle
                        .atribuiCentroCustoEmContasRecebeOuPagar(lancamento)
                    journalEntriesService.update(lcComCentro,lcComCentro.JdtNum.toString())
                } catch (ex: Exception) {
                    journalEntriesService.update(lancamento,lancamento.JdtNum.toString())
                    logger.error("Erro ao atualizar o lançamento contábil ${lancamento.JdtNum}", ex)
                }
            }
        } catch (ex: Exception) {
            logger.error("Erro ao buscar e processar lançamentos contábeis", ex)
        }
    }
}