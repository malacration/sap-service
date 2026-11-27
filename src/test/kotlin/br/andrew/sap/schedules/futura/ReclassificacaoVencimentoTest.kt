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
import java.time.LocalDate

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

    private val hoje = LocalDate.of(2026, 8, 20)

    @Test
    fun `vence no ultimo boleto do contrato, cabecalho e linhas`() {
        whenever(adiantamentos.getLastInstallment(681)).doReturn("2027-01-16")

        val lancamento = schedule.novaReclassificacao(entrega(), hoje)

        assertEquals("2027-01-16", lancamento.DueDate)
        assertTrue(lancamento.journalEntryLines.all { it.DueDate == "2027-01-16" })
        val json = jacksonObjectMapper().writeValueAsString(lancamento)
        assertTrue(json.contains("\"DueDate\":\"2027-01-16\""), json)
    }

    @Test
    fun `contrato sem boleto vence hoje, cabecalho e linhas`() {
        whenever(adiantamentos.getLastInstallment(681)).doReturn(null)

        val lancamento = schedule.novaReclassificacao(entrega(), hoje)

        assertEquals("2026-08-20", lancamento.DueDate)
        assertTrue(lancamento.journalEntryLines.isNotEmpty())
        assertTrue(lancamento.journalEntryLines.all { it.DueDate == "2026-08-20" })
        val json = jacksonObjectMapper().writeValueAsString(lancamento)
        assertTrue(json.contains("\"DueDate\":\"2026-08-20\""), json)
    }

    @Test
    fun `lancamento lido do SAP nao reenvia o vencimento no PATCH`() {
        val doSap = """{"JdtNum":1,"Memo":"m","DueDate":"2026-11-14","JournalEntryLines":
            [{"AccountCode":"1.1","Debit":1.0,"Credit":0.0,"BPLID":2,"Line_ID":0,"DueDate":"2026-11-14"}]}"""

        val lancamento = OData().mapper.readValue(doSap, JournalEntry::class.java)

        assertFalse(jacksonObjectMapper().writeValueAsString(lancamento).contains("DueDate"))
    }

    @Test
    fun `ultimo boleto ja vencido vence hoje, cabecalho e linhas`() {
        // boleto 08/06, entrega 20/08: com DueDate passado o cliente apareceria inadimplente
        whenever(adiantamentos.getLastInstallment(681)).doReturn("2026-06-08")

        val lancamento = schedule.novaReclassificacao(entrega(), hoje)

        assertEquals("2026-08-20", lancamento.DueDate)
        assertTrue(lancamento.journalEntryLines.all { it.DueDate == "2026-08-20" })
        val json = jacksonObjectMapper().writeValueAsString(lancamento)
        assertTrue(json.contains("\"DueDate\":\"2026-08-20\""), json)
        assertFalse(json.contains("2026-06-08"), json)
    }

    @Test
    fun `vencimento - boleto futuro e mantido`() {
        assertEquals("2027-01-16",
            ReclassificacaoEntregaVendaFuturaSchedule.vencimentoReclassificacao("2027-01-16", hoje))
    }

    @Test
    fun `vencimento - boleto passado vira hoje`() {
        assertEquals("2026-08-20",
            ReclassificacaoEntregaVendaFuturaSchedule.vencimentoReclassificacao("2026-06-08", hoje))
    }

    @Test
    fun `vencimento - boleto igual a hoje fica hoje`() {
        assertEquals("2026-08-20",
            ReclassificacaoEntregaVendaFuturaSchedule.vencimentoReclassificacao("2026-08-20", hoje))
    }

    @Test
    fun `vencimento - boleto de ontem vira hoje`() {
        assertEquals("2026-08-20",
            ReclassificacaoEntregaVendaFuturaSchedule.vencimentoReclassificacao("2026-08-19", hoje))
    }

    @Test
    fun `vencimento - sem boleto vence hoje`() {
        assertEquals("2026-08-20",
            ReclassificacaoEntregaVendaFuturaSchedule.vencimentoReclassificacao(null, hoje))
    }

    @Test
    fun `vencimento - data com hora usa so o dia`() {
        assertEquals("2026-11-01",
            ReclassificacaoEntregaVendaFuturaSchedule.vencimentoReclassificacao("2026-11-01T00:00:00", hoje))
        assertEquals("2026-08-20",
            ReclassificacaoEntregaVendaFuturaSchedule.vencimentoReclassificacao("2026-06-08T23:59:59", hoje))
    }
}
