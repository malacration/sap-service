package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.dto.InstallmentPixConsulta
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.document.DownPaymentService
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
        val downPaymentService = mock(DownPaymentService::class.java)
        val pixPaymentVerificationService = mock(PixPaymentVerificationService::class.java)
        val schedule = PixPaymentSchedule(invoiceService, downPaymentService, pixPaymentVerificationService, 15)
        val proximaConsultaFutura = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES)

        whenever(invoiceService.getPixsGeradosParaConsulta(any())).thenReturn(
            listOf(
                InstallmentPixConsulta(10, 1),
                InstallmentPixConsulta(10, 2)
            )
        )
        whenever(downPaymentService.getPixsGeradosParaConsulta(any())).thenReturn(listOf())
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

    @Test
    fun execute_deveAtualizarPixDeAdiantamentoElegivelParaConsulta() {
        val invoiceService = mock(InvoiceService::class.java)
        val downPaymentService = mock(DownPaymentService::class.java)
        val pixPaymentVerificationService = mock(PixPaymentVerificationService::class.java)
        val schedule = PixPaymentSchedule(invoiceService, downPaymentService, pixPaymentVerificationService, 15)
        val proximaConsultaFutura = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES)

        whenever(invoiceService.getPixsGeradosParaConsulta(any())).thenReturn(listOf())
        whenever(downPaymentService.getPixsGeradosParaConsulta(any())).thenReturn(
            listOf(InstallmentPixConsulta(20, 1), InstallmentPixConsulta(20, 2))
        )
        whenever(downPaymentService.getById(20)).thenReturn(
            OData(
                linkedMapOf(
                    "value" to """
                    {
                      "CardCode":"C20000",
                      "BPL_IDAssignedToInvoice":"1",
                      "DocEntry":20,
                      "DocumentInstallments":[
                        {"InstallmentId":1,"U_pix_reference":"ref-a1"},
                        {"InstallmentId":2,"U_pix_reference":"ref-a2","U_pix_proxima_consulta_em":"$proximaConsultaFutura"}
                      ]
                    }
                    """.trimIndent()
                )
            )
        )
        whenever(
            pixPaymentVerificationService.verificaPixEhBaixa(any<DownPayment>(), any<Installment>())
        ).thenReturn(Transaction("ref-a1"))

        schedule.execute()

        val installmentsCaptor = argumentCaptor<List<Installment>>()
        verify(downPaymentService).updatePixInstallments(eq(20), installmentsCaptor.capture())
        verify(pixPaymentVerificationService, times(1)).verificaPixEhBaixa(any<DownPayment>(), any<Installment>())

        val installmentsAtualizadas = installmentsCaptor.firstValue
        assertEquals(listOf(1), installmentsAtualizadas.mapNotNull { it.InstallmentId })
        assertEquals("ref-a1", installmentsAtualizadas.first().U_pix_reference)
        assertNotNull(installmentsAtualizadas.first().U_pix_proxima_consulta_em)
    }
}
