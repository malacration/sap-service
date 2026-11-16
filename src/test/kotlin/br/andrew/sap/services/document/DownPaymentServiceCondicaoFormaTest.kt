package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.documents.AccountsReceivableService
import br.andrew.sap.services.documents.CreditNotesService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.infrastructure.odata.Parameter
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDate

/**
 * O adiantamento (boleto) do contrato sai com a condicao e a forma do pedido de origem - as mesmas
 * da minuta. Antes o SAP preenchia com as do cadastro do cliente (contrato 713: pedido "60" na
 * conta 1199, boleto "150" na conta 126455).
 */
class DownPaymentServiceCondicaoFormaTest {

    private val orderService = mock(OrdersService::class.java)
    private val sql = mock(SqlQueriesService::class.java)
    private val service = spy(DownPaymentService(
        mock(SapEnvrioment::class.java),
        sql,
        orderService,
        mock(PaymentTermsTypesService::class.java),
        mock(BankPlusService::class.java),
        mock(AccountsReceivableService::class.java),
        mock(IncomingPaymentService::class.java),
        mock(CreditNotesService::class.java),
        mock(BatchService::class.java),
        mock(JournalEntriesService::class.java),
        mock(DynamicPixQrCodeService::class.java),
        mock(BusinessPartnersService::class.java),
        "VF-ITEM",
        66,
        "FORMA-CONFIG",
        mock(RestTemplate::class.java),
        mock(AuthService::class.java)
    ))

    private val contrato = Contrato(99, "C0001", mutableListOf(Item("A", "A", 10.0, 1.0, 10.0, 0.0, 1.0, "UN")), 1, "Cliente", 2)
        .also { it.DocEntry = 713 }
    private val parcela = PaymentDueDates(BigDecimal("100.00"), LocalDate.of(2027, 1, 16))

    private fun pedido(condicao: Int?, forma: String?) =
        OrderSales("C0001", "2026-11-16", listOf()).also { it.paymentGroupCode = condicao; it.paymentMethod = forma }

    @Test
    fun `usa condicao e forma do pedido`() {
        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela, pedido(13, "BB-RC-BOL-1199"))

        assertEquals(13, adiantamento.paymentGroupCode)
        assertEquals("BB-RC-BOL-1199", adiantamento.paymentMethod)
    }

    @Test
    fun `pedido sem forma cai na forma configurada`() {
        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela, pedido(13, " "))

        assertEquals("FORMA-CONFIG", adiantamento.paymentMethod)
    }

    @Test
    fun `sem pedido informado busca o pedido do contrato - troca`() {
        val odata = OData(linkedMapOf("value" to OData().mapper.writeValueAsString(pedido(13, "BB-RC-BOL-1199"))))
        whenever(orderService.getById(99)).doReturn(odata)

        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela)

        assertEquals(13, adiantamento.paymentGroupCode)
        assertEquals("BB-RC-BOL-1199", adiantamento.paymentMethod)
    }

    @Test
    fun `pedido sem condicao usa a do contrato, nao a do cadastro do cliente`() {
        contrato.U_condicaoPagamento = 13

        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela, pedido(null, "BB-RC-BOL-1199"))

        assertEquals(13, adiantamento.paymentGroupCode)
    }

    @Test
    fun `ultimo boleto ignora o devolvido - estorno de PIX expirado nao desliga do contrato`() {
        fun boleto(entry: Int, vencimento: String) =
            Document("C0001", vencimento, listOf(), "2").also { it.docEntry = entry }
        doReturn(listOf(boleto(2, "2027-03-10T00:00:00Z"), boleto(1, "2027-01-16T00:00:00Z")))
            .whenever(service).getByContratoVendaFutura(eq(713), any())
        val devolvido = OData(linkedMapOf("value" to """[{"DocEntry":900}]"""))
        whenever(sql.execute(eq("devolucao-adiantamento.sql"), any<Parameter>()))
            .thenAnswer { if ((it.arguments[1] as Parameter).value == 2) devolvido else null }

        assertEquals("2027-01-16", service.getLastInstallment(713))
    }
}
