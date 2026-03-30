package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.dto.InstallmentPixConsulta
import br.andrew.sap.schedules.pix.DownPaymentPixExpirationSchedule
import br.andrew.sap.services.document.DownPaymentService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class DownPaymentPixExpirationScheduleTest {

    @Test
    fun execute_deveCancelarAdiantamentoCompletoQuandoHouverPixVencido() {
        val downPaymentService = mock(DownPaymentService::class.java)
        val schedule = DownPaymentPixExpirationSchedule(downPaymentService)
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

        schedule.execute()

        verify(downPaymentService, times(1)).cancel(eq("30"))
        verify(downPaymentService, never()).updatePixInstallments(any(), any())
    }
}
