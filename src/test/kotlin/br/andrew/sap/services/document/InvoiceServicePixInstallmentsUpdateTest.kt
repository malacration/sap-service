package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.dto.InvoicePixUpdatePayload
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Session
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

class InvoiceServicePixInstallmentsUpdateTest {

    @Test
    fun updatePixInstallments_deveEnviarSomentePayloadDasParcelas() {
        val restTemplate = mock(RestTemplate::class.java)
        whenever(
            restTemplate.exchange(
                any<RequestEntity<*>>(),
                eq(OData::class.java)
            )
        ).thenReturn(ResponseEntity.ok(OData()))

        val authService = mock(AuthService::class.java)
        whenever(authService.getToken(any())).thenReturn(Session("sessao", "", 30))

        val service = InvoiceService(
            SapEnvrioment("https://sap.local", "user", "password", "SBODEMO"),
            restTemplate,
            authService,
            mock(BussinessPlaceService::class.java),
            mock(DynamicPixQrCodeService::class.java),
            mock(BusinessPartnersService::class.java),
            mock(IncomingPaymentService::class.java),
            mock(TransactionsPixService::class.java),
            mock(BankPlusService::class.java),
            mock(AccountsReceivableService::class.java),
            mock(BatchService::class.java),
            mock(JournalEntriesService::class.java),
            mock(SqlQueriesService::class.java)
        )

        val installment = Installment(LocalDate.parse("2026-03-20"), 1200.0).also {
            it.DocEntry = 999
            it.InstallmentId = 2
            it.U_QrCodePix = "qr-code"
            it.U_pix_textContent = "pix-text"
            it.U_pix_link = "https://pix.example"
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = "2026-03-20"
            it.U_pix_proxima_consulta_em = "2026-03-19T10:00"
            it.U_pix_consultar_ate = "2026-03-20T23:59:59.999999999"
        }

        service.updatePixInstallments(10, listOf(installment))

        val requestCaptor = argumentCaptor<RequestEntity<*>>()
        verify(restTemplate).exchange(requestCaptor.capture(), eq(OData::class.java))

        val request = requestCaptor.firstValue
        val body = request.body

        assertTrue(body is InvoicePixUpdatePayload)
        assertFalse(body is Invoice)
        assertEquals("B1SESSION=sessao", request.headers.getFirst("cookie"))

        val payload = body as InvoicePixUpdatePayload
        assertEquals(1, payload.DocumentInstallments.size)
        assertEquals(2, payload.DocumentInstallments.first().InstallmentId)
        assertEquals("ref-123", payload.DocumentInstallments.first().pixReference)
        assertEquals("2026-03-19T10:00", payload.DocumentInstallments.first().pixProximaConsultaEm)
    }
}
