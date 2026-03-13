package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.dto.InstallmentPixConsulta
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.uzzipay.PixPaymentVerificationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class PixPaymentScheduleTest {

    @Test
    fun execute_deveAtualizarSomenteParcelasElegiveisParaConsulta() {
        val invoiceService = mock(InvoiceService::class.java)
        val pixPaymentVerificationService = mock(PixPaymentVerificationService::class.java)
        val schedule = PixPaymentSchedule(invoiceService, pixPaymentVerificationService, 15)
        val proximaConsultaFutura = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES)

        whenever(invoiceService.getPixsGeradosParaConsulta(any())).thenReturn(
            listOf(
                InstallmentPixConsulta(10, 1),
                InstallmentPixConsulta(10, 2)
            )
        )
        whenever(invoiceService.getById(10)).thenReturn(
            OData(
                linkedMapOf(
                    "value" to """
                    {
                      "CardCode":"C20000",
                      "BPL_IDAssignedToInvoice":"1",
                      "DocEntry":10,
                      "DocumentInstallments":[
                        {"InstallmentId":1,"U_pix_reference":"ref-1"},
                        {"InstallmentId":2,"U_pix_reference":"ref-2","U_pix_proxima_consulta_em":"$proximaConsultaFutura"}
                      ]
                    }
                    """.trimIndent()
                )
            )
        )
        whenever(
            pixPaymentVerificationService.verificaPixEhBaixa(any<Invoice>(), any<Installment>())
        ).thenReturn(Transaction("ref-1"))

        schedule.execute()

        val installmentsCaptor = argumentCaptor<List<Installment>>()
        verify(invoiceService).updatePixInstallments(eq(10), installmentsCaptor.capture())
        verify(pixPaymentVerificationService, times(1)).verificaPixEhBaixa(any<Invoice>(), any<Installment>())

        val installmentsAtualizadas = installmentsCaptor.firstValue
        assertEquals(listOf(1), installmentsAtualizadas.mapNotNull { it.InstallmentId })
        assertEquals("ref-1", installmentsAtualizadas.first().U_pix_reference)
        assertNotNull(installmentsAtualizadas.first().U_pix_proxima_consulta_em)
    }
}
