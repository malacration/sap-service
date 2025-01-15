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
@ConditionalOnProperty(value = ["lc.gpcc.enable"], havingValue = "true", matchIfMissing = false)
class AtualizarObservacaoGrupoCP(
    private val journalEntriesService: JournalEntriesService,
    private val journalMemoHandle: JournalMemoHandle,
    @Value("\${lc.memo.dias:5}") private val dias: Long,
    private val currentSapUser: SapUser
) {

    private val logger: Logger = LoggerFactory.getLogger(AtualizarObservacaoGrupoCP::class.java)

    @Scheduled(fixedDelay = 15000)
    fun execute() {
        val filter = Filter(
            Predicate("U_Atualizar_Observacao_Grupo_CP", 0, Condicao.EQUAL),
            Predicate("JdtNum", 803560, Condicao.EQUAL)
        )

        val resultados = journalEntriesService.get(filter, OrderBy("ReferenceDate", Order.DESC))
            .tryGetPageValues<JournalEntry>(Pageable.unpaged())

        resultados.forEach { journalEntry ->
            try {
                logger.info("Atualizando memo para o jornal ${journalEntry.JdtNum}")
                journalMemoHandle.updateGrupoEconomicoCentroCusto(journalEntry.JdtNum!!)
            } catch (e: Exception) {
                logger.error("Erro ao atualizar a descrição do LC ${journalEntry.JdtNum}", e)
            }
        }
    }
}
