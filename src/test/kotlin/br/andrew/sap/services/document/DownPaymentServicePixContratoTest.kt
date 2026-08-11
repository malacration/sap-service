package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestTemplate

/**
 * A taxa de mora chega ao endpoint e precisa atravessar ate a geracao do QR Code — era
 * exatamente esse repasse que faltava e fazia o contrato de venda futura sair sem juros.
 */
class DownPaymentServicePixContratoTest {

    private val service = spy(
        DownPaymentService(
            mock(SapEnvrioment::class.java),
            mock(SqlQueriesService::class.java),
            mock(OrdersService::class.java),
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
            "none",
            mock(RestTemplate::class.java),
            mock(AuthService::class.java)
        )
    )

    @Test
    fun `repassa a taxa de mora para a geracao do pix e para a montagem dos boletos`() {
        prepara()

        service.createPixByContratoVendaFutura(10, 0.1)

        verify(service).createPix(any(), eq(listOf()), eq(0.1))
        verify(service).getByContratoVendaFuturaStatus(10, 0.1)
    }

    @Test
    fun `sem taxa informada mantem o comportamento sem juros`() {
        prepara()

        service.createPixByContratoVendaFutura(10)

        verify(service).createPix(any(), eq(listOf()), eq(0.0))
        verify(service).getByContratoVendaFuturaStatus(10, 0.0)
    }

    @Test
    fun `ignora adiantamento fechado ou cancelado`() {
        val fechado = adiantamentoDoContrato().also { it.DocumentStatus = DocumentStatus.bost_Close }
        val cancelado = adiantamentoDoContrato().also { it.Cancelled = Cancelled.tYES }
        doReturn(listOf<Document>(fechado, cancelado)).whenever(service).getByContratoVendaFutura(eq(10), any())
        doReturn(listOf<br.andrew.sap.model.self.vendafutura.BoletoVf>())
            .whenever(service).getByContratoVendaFuturaStatus(eq(10), any())

        service.createPixByContratoVendaFutura(10, 0.1)

        verify(service, never()).createPix(any(), any(), any())
    }

    private fun prepara() {
        doReturn(listOf<Document>(adiantamentoDoContrato()))
            .whenever(service).getByContratoVendaFutura(eq(10), any())
        doReturn(odataAdiantamento()).whenever(service).getById(321)
        doReturn(listOf<br.andrew.sap.model.sap.documents.base.Installment>())
            .whenever(service).createPix(any(), any(), any())
        doReturn(listOf<br.andrew.sap.model.self.vendafutura.BoletoVf>())
            .whenever(service).getByContratoVendaFuturaStatus(eq(10), any())
    }

    private fun adiantamentoDoContrato(): DownPayment {
        return DownPayment(
            "C-001",
            "2026-07-16",
            listOf(Product("VF-ITEM", "1", "1000.00", 66)),
            "3"
        ).also {
            it.docEntry = 321
            it.docNum = "654"
            it.docObjectCode = DocumentTypes.oDownPayments
            it.DocumentStatus = DocumentStatus.bost_Open
            it.U_venda_futura = 10
        }
    }

    private fun odataAdiantamento(): OData {
        val backing = LinkedHashMap<String, Any?>()
        backing["value"] = """{"CardCode":"C-001","BPL_IDAssignedToInvoice":"3","DocEntry":321}"""
        return OData(backing)
    }
}
