package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.CreditNotes
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestTemplate

class DownPaymentServiceEstornoTest {

    private val sqlQueriesService = mock(SqlQueriesService::class.java)
    private val creditNotesService = mock(CreditNotesService::class.java)

    private val service = DownPaymentService(
        mock(SapEnvrioment::class.java),
        sqlQueriesService,
        mock(OrdersService::class.java),
        mock(PaymentTermsTypesService::class.java),
        mock(BankPlusService::class.java),
        mock(AccountsReceivableService::class.java),
        mock(IncomingPaymentService::class.java),
        creditNotesService,
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

    @Test
    fun estornarPorDevolucaoUsaMotivoDefaultDePixExpirado() {
        whenever(sqlQueriesService.execute(eq("devolucao-adiantamento.sql"), any<Parameter>()))
            .thenReturn(null)
        whenever(creditNotesService.save(any())).thenReturn(odataDevolucao())

        service.estornarPorDevolucao(adiantamento())

        val captor = argumentCaptor<CreditNotes>()
        verify(creditNotesService).save(captor.capture())
        assertEquals(
            "Estorno automatico de adiantamento PIX expirado. Adiantamento 654",
            captor.firstValue.comments
        )
    }

    @Test
    fun estornarPorDevolucaoComMotivoCustomizado() {
        whenever(sqlQueriesService.execute(eq("devolucao-adiantamento.sql"), any<Parameter>()))
            .thenReturn(null)
        whenever(creditNotesService.save(any())).thenReturn(odataDevolucao())

        service.estornarPorDevolucao(adiantamento(), "Estorno automatico por falha ao gravar dados do PIX.")

        val captor = argumentCaptor<CreditNotes>()
        verify(creditNotesService).save(captor.capture())
        assertEquals(
            "Estorno automatico por falha ao gravar dados do PIX. Adiantamento 654",
            captor.firstValue.comments
        )
    }

    @Test
    fun estornarPorDevolucaoEhIdempotenteQuandoJaExisteDevolucao() {
        whenever(sqlQueriesService.execute(eq("devolucao-adiantamento.sql"), any<Parameter>()))
            .thenReturn(odata(listOf(mapOf("DocEntry" to 999))))
        whenever(creditNotesService.getById(999)).thenReturn(odataDevolucao())

        val devolucao = service.estornarPorDevolucao(adiantamento())

        assertEquals(999, devolucao.docEntry)
        verify(creditNotesService, never()).save(any())
    }

    private fun adiantamento(): DownPayment {
        return DownPayment(
            "C-001",
            "2026-07-16",
            listOf(Product("PIX-ITEM", "1", "82.00", 66)),
            "3"
        ).also {
            it.docEntry = 321
            it.docNum = "654"
            it.docObjectCode = DocumentTypes.oDownPayments
        }
    }

    private fun odataDevolucao(): OData {
        return odata("""{"CardCode":"C-001","BPL_IDAssignedToInvoice":"3","DocEntry":999}""")
    }

    private fun odata(value: Any?): OData {
        val backing = LinkedHashMap<String, Any?>()
        backing["value"] = value
        return OData(backing)
    }
}
