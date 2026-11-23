package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.StatusPagamentoBoleto
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestTemplate

/** Status por ODPI (casado por DocEntry): devolvido/pago/parcial/aberto, ou nulo quando falta dado. */
class DownPaymentServiceStatusBoletoTest {

    private val sql = mock(SqlQueriesService::class.java)
    private val service = spy(
        DownPaymentService(
            mock(SapEnvrioment::class.java), sql,
            mock(OrdersService::class.java), mock(PaymentTermsTypesService::class.java),
            mock(BankPlusService::class.java), mock(AccountsReceivableService::class.java),
            mock(IncomingPaymentService::class.java), mock(CreditNotesService::class.java),
            mock(BatchService::class.java), mock(JournalEntriesService::class.java),
            mock(DynamicPixQrCodeService::class.java), mock(BusinessPartnersService::class.java),
            "VF-ITEM", 66, "none", mock(RestTemplate::class.java), mock(AuthService::class.java)
        )
    )

    private fun prepara(docs: List<DownPayment>, rows: List<Map<String, Any?>>, parcelas: Set<Int> = setOf()) {
        doReturn(docs).whenever(service).getByContratoVendaFutura(eq(10), any())
        docs.forEach { doReturn(odata(it, it.docEntry in parcelas)).whenever(service).getById(it.docEntry!!) }
        doReturn(OData(linkedMapOf("value" to rows)))
            .whenever(sql).execute(eq("boletos-status.sql"), any<List<br.andrew.sap.infrastructure.odata.Parameter>>())
    }

    private fun statusDe(docEntry: Int) =
        service.getByContratoVendaFuturaStatus(10).filter { it.DocEntry == docEntry }

    @Test
    fun `aberto parcial pago e devolvido caem no boleto do proprio adiantamento`() {
        prepara(
            listOf(adiantamento(1, 11), adiantamento(2, 12), adiantamento(3, 13), adiantamento(4, 14)),
            listOf(
                linha(1, paid = 0.0),
                linha(2, paid = 400.0, recebimento = 700),
                linha(3, paid = 1000.0, recebimento = 701),
                linha(4, paid = 1000.0, recebimento = 702, devolucao = 900)  // DEVOLVIDO vence PAGO
            ),
            parcelas = setOf(2)
        )

        val boletos = service.getByContratoVendaFuturaStatus(10)

        assertEquals(5, boletos.size)
        val porDoc = boletos.groupBy { it.DocEntry }
        assertEquals(listOf(StatusPagamentoBoleto.ABERTO), porDoc[1]!!.map { it.StatusPagamento })
        assertEquals(0.0, porDoc[1]!!.single().PaidToDate)
        assertNull(porDoc[1]!!.single().devolucao)
        // o valor recebido e do ODPI: as duas parcelas herdam o mesmo status
        assertEquals(listOf(StatusPagamentoBoleto.PARCIAL, StatusPagamentoBoleto.PARCIAL), porDoc[2]!!.map { it.StatusPagamento })
        assertEquals(400.0, porDoc[2]!!.first().PaidToDate)
        assertEquals("700", porDoc[2]!!.first().recebimento)
        assertEquals(StatusPagamentoBoleto.PAGO, porDoc[3]!!.single().StatusPagamento)
        assertEquals(StatusPagamentoBoleto.DEVOLVIDO, porDoc[4]!!.single().StatusPagamento)
        assertEquals("900", porDoc[4]!!.single().devolucao)
    }

    @Test
    fun `falta de ate um centavo conta como pago`() {
        prepara(
            listOf(adiantamento(1, 11), adiantamento(2, 12), adiantamento(3, 13)),
            listOf(
                linha(1, paid = 46799.99, total = 46800.0),
                linha(2, paid = 46799.98, total = 46800.0),
                linha(3, paid = 0.01, total = 46800.0)
            )
        )
        assertEquals(StatusPagamentoBoleto.PAGO, statusDe(1).single().StatusPagamento)
        assertEquals(StatusPagamentoBoleto.PARCIAL, statusDe(2).single().StatusPagamento)
        assertEquals(StatusPagamentoBoleto.PARCIAL, statusDe(3).single().StatusPagamento)
    }

    @Test
    fun `limites decimais sem arredondamento de centavo`() {
        prepara(
            listOf(adiantamento(1, 11), adiantamento(2, 12)),
            listOf(
                linha(1, paid = 0.0, total = 0.01),      // falta 0.01 => PAGO antes de ABERTO
                linha(2, paid = 99.986, total = 100.0)   // falta 0.014 => PARCIAL
            )
        )
        assertEquals(StatusPagamentoBoleto.PAGO, statusDe(1).single().StatusPagamento)
        assertEquals(StatusPagamentoBoleto.PARCIAL, statusDe(2).single().StatusPagamento)
    }

    @Test
    fun `primeira pagina sem corpo mantem boletos com status nulo`() {
        val docs = listOf(adiantamento(1, 11))
        prepara(docs, listOf())
        doReturn(null).whenever(sql).execute(eq("boletos-status.sql"), any<List<br.andrew.sap.infrastructure.odata.Parameter>>())
        val boletos = service.getByContratoVendaFuturaStatus(10)
        assertEquals(1, boletos.size)
        assertNull(boletos.single().StatusPagamento)
    }

    @Test
    fun `continuacao sem corpo mantem linhas ja lidas`() {
        prepara(listOf(adiantamento(1, 11), adiantamento(2, 12)), listOf(linha(1, paid = 0.0)))
        doReturn(OData(linkedMapOf("value" to listOf(linha(1, paid = 0.0)), "odata.nextLink" to "next?skip=1")))
            .whenever(sql).execute(eq("boletos-status.sql"), any<List<br.andrew.sap.infrastructure.odata.Parameter>>())
        doReturn(null).whenever(sql).nextLink(eq("next?skip=1"))
        assertEquals(StatusPagamentoBoleto.ABERTO, statusDe(1).single().StatusPagamento)
        assertNull(statusDe(2).single().StatusPagamento)
    }

    @Test
    fun `dado ausente deixa status nulo e zero real e aberto`() {
        prepara(
            listOf(adiantamento(1, 11), adiantamento(2, 12), adiantamento(3, 13), adiantamento(4, 14)),
            listOf(  // docEntry 1 sem linha no SQL
                linha(2, paid = null),
                linha(3, paid = 500.0).minus("DocTotal"),
                linha(4, paid = 0.0)
            )
        )
        assertNull(statusDe(1).single().StatusPagamento)
        assertNull(statusDe(1).single().PaidToDate)
        assertNull(statusDe(2).single().StatusPagamento)
        assertNull(statusDe(3).single().StatusPagamento)
        assertEquals(StatusPagamentoBoleto.ABERTO, statusDe(4).single().StatusPagamento)
    }

    @Test
    fun `docnum repetido em series diferentes casa por docentry`() {
        prepara(
            listOf(adiantamento(1, 50), adiantamento(2, 50)),
            listOf(linha(1, paid = 1000.0, docNum = 50), linha(2, paid = 0.0, docNum = 50))
        )
        assertEquals(StatusPagamentoBoleto.PAGO, statusDe(1).single().StatusPagamento)
        assertEquals(StatusPagamentoBoleto.ABERTO, statusDe(2).single().StatusPagamento)
    }

    private fun linha(
        docEntry: Int, paid: Double?, recebimento: Int? = null, devolucao: Int? = null,
        docNum: Int = docEntry + 10, total: Double = 1000.0
    ) = mapOf(
        "DocEntry" to docEntry, "DocNum" to docNum.toString(), "DocTotal" to total,
        "PaidToDate" to paid, "Recebimento" to recebimento, "Devolucao" to devolucao, "DocStatus" to "bost_Open"
    )

    private fun adiantamento(docEntry: Int, docNum: Int) =
        DownPayment("C-001", "2026-07-16", listOf(Product("VF-ITEM", "1", "1000.00", 66)), "3").also {
            it.docEntry = docEntry
            it.docNum = docNum.toString()
            it.docObjectCode = DocumentTypes.oDownPayments
            it.DocumentStatus = DocumentStatus.bost_Open
            it.U_venda_futura = 10
        }

    private fun odata(d: DownPayment, parcelas: Boolean): OData {
        val inst = if (parcelas)
            ""","DocumentInstallments":[{"DueDate":"2026-07-20","Total":500.0,"InstallmentId":1},{"DueDate":"2026-08-20","Total":500.0,"InstallmentId":2}]"""
        else ""
        return OData(linkedMapOf("value" to
            """{"CardCode":"C-001","BPL_IDAssignedToInvoice":"3","DocEntry":${d.docEntry},"DocNum":"${d.docNum}","DocTotal":"1000.00"$inst}"""))
    }
}
