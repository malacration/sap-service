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
@ConditionalOnProperty(value = ["lc.memo.enable"], havingValue = "true", matchIfMissing = false)
class AtualizarObservacaoLancamento(
    private val journalEntriesService: JournalEntriesService,
    private val journalMemoHandle: JournalMemoHandle,
    @Value("\${lc.memo.dias:5}") private val dias: Long,
    private val currentSapUser: SapUser
) {

    val logger: Logger = LoggerFactory.getLogger(AtualizarObservacaoLancamento::class.java)

    @Scheduled(fixedDelay = 60000)
    fun execute() {
        val dataLimite = LocalDate.now().minusDays(dias).toString()
        val filter = Filter(
            Predicate("U_Atualizar_Observacao", 0, Condicao.EQUAL),
            Predicate("ReferenceDate", dataLimite, Condicao.GREAT_EQUAL)
        )
        val resultado = journalEntriesService.get(filter,OrderBy("ReferenceDate", Order.DESC))
            .tryGetPageValues<JournalEntry>(Pageable.unpaged())
        resultado.forEach { journalEntry ->
            try{
                logger.info("Atualizando memo para o jornal ${journalEntry.JdtNum}")
                journalMemoHandle.updateMemoJournal(journalEntry.JdtNum!!)
            }catch (e : Exception){
                logger.error("Erro ao atualizar a descricao do LC ${journalEntry.JdtNum}",e)
            }
        }
    }
}
