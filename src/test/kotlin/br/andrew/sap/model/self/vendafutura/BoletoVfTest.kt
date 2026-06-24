package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BoletoVfTest {

    @Test
    fun `mapeia dados pix diretamente da parcela do adiantamento`() {
        val installment = Installment(LocalDate.of(2026, 6, 20), 1500.0).also {
            it.InstallmentId = 1
            it.U_QrCodePix = "pix-copia-cola"
            it.U_pix_textContent = "pix-copia-cola"
            it.U_pix_link = "https://pix"
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = "2026-06-20"
            it.U_pix_consultar_ate = "2026-06-20T23:59:59"
        }
        val downPayment = Document("C1", "2026-06-20", listOf(), "2").also {
            it.docEntry = 123
            it.docNum = "456"
            it.DocTotal = "1500.00"
            it.DocumentStatus = DocumentStatus.bost_Open
        }

        val boleto = BoletoVf.from(downPayment, installment)

        assertEquals(123, boleto.DocEntry)
        assertEquals(1, boleto.InstallmentId)
        assertEquals("O", boleto.DocStatus)
        assertEquals("ref-123", boleto.U_pix_reference)
        assertEquals("pix-copia-cola", boleto.U_pix_textContent)
        // o DTO envia a validade digital (consultar_ate) no campo due_date consumido pelo front
        assertEquals("2026-06-20T23:59:59", boleto.U_pix_due_date)
        assertEquals("2026-06-20T23:59:59", boleto.U_pix_consultar_ate)
    }
}
