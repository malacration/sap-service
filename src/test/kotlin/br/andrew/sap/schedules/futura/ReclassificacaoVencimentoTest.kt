package br.andrew.sap.schedules.futura

import JournalEntry
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.services.documents.DownPaymentService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * A reclassificacao (VFET) fica em aberto no parceiro ate a apropriacao. Com o vencimento do dia
 * da entrega o cliente aparecia vencido sem estar; ela vence junto com o ultimo boleto do contrato.
 */
class ReclassificacaoVencimentoTest {

    private val adiantamentos = mock<DownPaymentService>()
    private val schedule = ReclassificacaoEntregaVendaFuturaSchedule(
        mock(), mock(), mock(), mock(), mock(), mock(), mock(), adiantamentos, mock(), mock(),
        listOf(2), "VF-ITEM", 79, "2.1.1.01"
    )

    private fun entrega() = Invoice("C0001", "2026-11-14", listOf(), "2").also {
        it.U_venda_futura = 681
        it.DocTotal = "146.00"
        it.controlAccount = "1.1.2.01"
        it.docNum = "76045"
    }

    @Test
    fun `vence no ultimo boleto do contrato, cabecalho e linhas`() {
        whenever(adiantamentos.getLastInstallment(681)).doReturn("2027-01-16")

        val lancamento = schedule.novaReclassificacao(entrega())

        assertEquals("2027-01-16", lancamento.DueDate)
        assertTrue(lancamento.journalEntryLines.all { it.DueDate == "2027-01-16" })
        val json = jacksonObjectMapper().writeValueAsString(lancamento)
        assertTrue(json.contains("\"DueDate\":\"2027-01-16\""), json)
    }

    @Test
    fun `contrato sem boleto nao envia vencimento - SAP usa a data do lancamento`() {
        whenever(adiantamentos.getLastInstallment(681)).doReturn(null)

        val json = jacksonObjectMapper().writeValueAsString(schedule.novaReclassificacao(entrega()))

        assertFalse(json.contains("DueDate"), json)
    }

    @Test
    fun `lancamento lido do SAP nao reenvia o vencimento no PATCH`() {
        val doSap = """{"JdtNum":1,"Memo":"m","DueDate":"2026-11-14","JournalEntryLines":
            [{"AccountCode":"1.1","Debit":1.0,"Credit":0.0,"BPLID":2,"Line_ID":0,"DueDate":"2026-11-14"}]}"""

        val lancamento = OData().mapper.readValue(doSap, JournalEntry::class.java)

        assertFalse(jacksonObjectMapper().writeValueAsString(lancamento).contains("DueDate"))
    }
}
