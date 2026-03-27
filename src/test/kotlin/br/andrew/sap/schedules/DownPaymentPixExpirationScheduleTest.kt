package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.dto.InstallmentPixConsulta
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.uzzipay.PixPaymentVerificationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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

class DownPaymentPixExpirationScheduleTest {

    @Test
    fun execute_deveLimparPixVencidoNaoPagoDoAdiantamento() {
        val downPaymentService = mock(DownPaymentService::class.java)
        val pixPaymentVerificationService = mock(PixPaymentVerificationService::class.java)
        val schedule = DownPaymentPixExpirationSchedule(downPaymentService, pixPaymentVerificationService)
        val vencido = LocalDateTime.now().minusHours(2).truncatedTo(ChronoUnit.MINUTES)

        whenever(downPaymentService.getPixsVencidos(any())).thenReturn(
            listOf(InstallmentPixConsulta(30, 1))
        )
        whenever(downPaymentService.getById(30)).thenReturn(
            OData(
                linkedMapOf(
                    "value" to """
                    {
                      "CardCode":"C30000",
                      "BPL_IDAssignedToInvoice":"1",
                      "DocEntry":30,
                      "DocumentInstallments":[
                        {"InstallmentId":1,"U_pix_reference":"ref-exp","U_pix_due_date":"${vencido}","U_pix_consultar_ate":"${vencido}"}
                      ]
                    }
                    """.trimIndent()
                )
            )
        )
        whenever(
            pixPaymentVerificationService.verificaPixEhBaixa(any<DownPayment>(), any<Installment>())
        ).thenReturn(Transaction("ref-exp"))

        schedule.execute()

        val installmentsCaptor = argumentCaptor<List<Installment>>()
        verify(downPaymentService, times(1)).updatePixInstallments(eq(30), installmentsCaptor.capture())
        val installment = installmentsCaptor.firstValue.first()
        assertEquals(1, installment.InstallmentId)
        assertNull(installment.U_pix_reference)
        assertNull(installment.U_pix_textContent)
        assertNull(installment.U_pix_consultar_ate)
    }
}
