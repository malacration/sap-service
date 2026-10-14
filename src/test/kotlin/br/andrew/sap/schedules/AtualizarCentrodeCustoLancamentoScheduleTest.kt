package br.andrew.sap.schedules

import JournalEntry
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.journal.SemRateioNaOrigemException
import br.andrew.sap.model.sap.sistema.SapUser
import br.andrew.sap.services.cadastro.ProfitCenterService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.journal.JournalMemoHandle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * JdtNum 1507641: recebimento por conta, filial 4, sem NF para herdar a dimensao. O SAP recusa o
 * PATCH em conta de resultado sem rateio, entao o lancamento voltava a cada rodada do job.
 */
class AtualizarCentrodeCustoLancamentoScheduleTest {

    private val journalEntriesService = mock<JournalEntriesService>()
    private val journalMemoHandle = mock<JournalMemoHandle>()
    private val profitCenterService = mock<ProfitCenterService>()

    private fun recebimento() = mapOf(
        "JdtNum" to 1507641,
        "Memo" to "Recebimento",
        "OriginalJournal" to "ttReceipt",
        "Original" to 75153,
        "U_Atualizar_Centro_de_Custo" to 0,
        "JournalEntryLines" to listOf(
            mapOf("AccountCode" to "1.1.2.001.00999", "Debit" to 100.0, "Credit" to 0.0, "BPLID" to 4, "Line_ID" to 0),
            mapOf("AccountCode" to "3.1.3.001.00003", "Debit" to 0.0, "Credit" to 100.0, "BPLID" to 4, "Line_ID" to 1)
        )
    )

    private fun schedule(contas: List<String> = listOf("4.9.1.001.00001"), filial: Int = 2): AtualizarCentrodeCustoLancamentoSchedule {
        whenever(journalEntriesService.get(any(), any(), any())).thenReturn(OData(linkedMapOf("value" to listOf(recebimento()))))
        return AtualizarCentrodeCustoLancamentoSchedule(
            journalEntriesService, journalMemoHandle, 50, contas, filial, "500", "50000205",
            listOf("4:501:50100403"), profitCenterService, SapUser(1)
        )
    }

    private fun atualizados(vezes: Int): List<JournalEntry> {
        val capturado = argumentCaptor<Any>()
        verify(journalEntriesService, times(vezes)).update(capturado.capture(), any())
        return capturado.allValues.map { it as JournalEntry }
    }

    @Test
    fun `recebimento sem nota cai no padrao da filial na conta de resultado`() {
        whenever(journalMemoHandle.atribuiCentroCustoEmContasRecebeOuPagar(any()))
            .thenAnswer { throw SemRateioNaOrigemException("sem NF") }

        schedule().atualizarLancamentosContabeis()

        val lancamento = atualizados(1).single()
        assertEquals(1, lancamento.U_Atualizar_Centro_de_Custo)
        assertNull(lancamento.journalEntryLines[0].costingCode)
        assertEquals("501", lancamento.journalEntryLines[1].costingCode)
        assertEquals("50100403", lancamento.journalEntryLines[1].costingCode2)
    }

    @Test
    fun `falha transitoria na heranca nao grava padrao da filial`() {
        whenever(journalMemoHandle.atribuiCentroCustoEmContasRecebeOuPagar(any()))
            .thenThrow(RuntimeException("timeout no Service Layer"))

        schedule().atualizarLancamentosContabeis()

        atualizados(1).forEach { lancamento ->
            assertNull(lancamento.journalEntryLines[1].costingCode)
            assertNull(lancamento.journalEntryLines[1].costingCode2)
        }
    }

    @Test
    fun `regra especifica das contas tem prioridade sobre o padrao da filial`() {
        whenever(journalMemoHandle.atribuiCentroCustoEmContasRecebeOuPagar(any()))
            .thenAnswer { throw SemRateioNaOrigemException("sem NF") }

        schedule(contas = listOf("3.1.3.001.00003"), filial = 4).atualizarLancamentosContabeis()

        val lancamento = atualizados(1).single()
        assertEquals("500", lancamento.journalEntryLines[1].costingCode)
        assertEquals("50000205", lancamento.journalEntryLines[1].costingCode2)
    }
}
