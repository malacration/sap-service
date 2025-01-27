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
class AtualizarCentrodeCustoLancamento(
    val journalEntriesService: JournalEntriesService,
    val journalMemoHandle: JournalMemoHandle,
    @Value("\${lc.memo.dias:5}") private val dias: Long,
    val currentSapUser: SapUser
) {

    private val logger: Logger = LoggerFactory.getLogger(AtualizarCentrodeCustoLancamento::class.java)

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

            lancamentos.forEach { lancamento ->
                try {
                    lancamento.JdtNum?.let {
                        logger.info("Atualizando observação do lançamento contábil $it")
                        journalMemoHandle.atualizarGrupoEconomicoECentroCusto(it)
                    } ?: logger.warn("Lançamento contábil com JdtNum nulo. Ignorando processamento.")
                } catch (ex: Exception) {
                    logger.error("Erro ao atualizar o lançamento contábil ${lancamento.JdtNum}", ex)
                }
            }
        } catch (ex: Exception) {
            logger.error("Erro ao buscar e processar lançamentos contábeis", ex)
        }
    }
}