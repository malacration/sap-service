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
class AtualizarObservacaoGrupoCP(
    private val journalEntriesService: JournalEntriesService,
    private val journalMemoHandle: JournalMemoHandle,
    @Value("\${lc.memo.dias:5}") private val dias: Long,
    private val currentSapUser: SapUser
) {

    private val logger: Logger = LoggerFactory.getLogger(AtualizarObservacaoGrupoCP::class.java)

    @Scheduled(fixedDelay = 60000)
    fun atualizarLancamentosContabeis() {
        val dataLimite = LocalDate.now().minusDays(dias).toString()
        val filtroLancamentos = criarFiltroLancamentos(dataLimite)

        val lancamentos = journalEntriesService.get(filtroLancamentos, OrderBy("ReferenceDate", Order.DESC))
            .tryGetPageValues<JournalEntry>(Pageable.unpaged())

        lancamentos.forEach { lancamento ->
            processarLancamento(lancamento)
        }
    }

    private fun criarFiltroLancamentos(dataLimite: String): Filter {
        return Filter(
            Predicate("U_Atualizar_Observacao_Grupo_CP", 0, Condicao.EQUAL),
            Predicate("ReferenceDate", dataLimite, Condicao.GREAT_EQUAL)
        )
    }

    private fun processarLancamento(lancamento: JournalEntry) {
        try {
            logger.info("Atualizando observação do lançamento contábil ${lancamento.JdtNum}")
            lancamento.JdtNum?.let {
                journalMemoHandle.atualizarGrupoEconomicoECentroCusto(it)
            } ?: logger.warn("Lançamento contábil com JdtNum nulo. Ignorando processamento.")
        } catch (ex: Exception) {
            logger.error("Erro ao atualizar o lançamento contábil ${lancamento.JdtNum}", ex)
        }
    }
}
