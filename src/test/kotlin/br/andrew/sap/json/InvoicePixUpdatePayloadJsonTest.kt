package br.andrew.sap.json

import br.andrew.sap.model.dto.InvoicePixUpdatePayload
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Installment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InvoicePixUpdatePayloadJsonTest {

    @Test
    fun test() {
        val installment = Installment(LocalDate.parse("2026-03-20"), 1200.0).also {
            it.DocEntry = 123
            it.InstallmentId = 2
            it.Status = DocumentStatus.bost_Delivered
            it.U_QrCodePix = "qr-code"
            it.U_pix_textContent = "pix-text"
            it.U_pix_link = "https://pix.example"
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = "2026-03-20"
            it.U_pix_proxima_consulta_em = "2026-03-19T10:00"
            it.U_pix_consultar_ate = "2026-03-20T23:59:59.999999999"
        }
        val payload = InvoicePixUpdatePayload.from(listOf(installment))

        val json = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .writeValueAsString(payload)

        assertEquals(1, payload.DocumentInstallments.size)
        assertTrue(json.contains("\"DocumentInstallments\""))
        assertTrue(json.contains("\"U_QrCodePix\":\"qr-code\""))
        assertTrue(json.contains("U_pix_reference"))
        assertTrue(json.contains("\"InstallmentId\":2"))
        assertFalse(json.contains("\"DocEntry\":123"))
        assertFalse(json.contains("\"Status\""))
    }
}
