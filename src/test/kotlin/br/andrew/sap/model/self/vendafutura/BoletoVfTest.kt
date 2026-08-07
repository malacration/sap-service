package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun `sem taxa informada nao detalha juros e valor total e o nominal`() {
        val installment = Installment(LocalDate.now().minusDays(10), 1000.0).also { it.InstallmentId = 1 }
        val downPayment = adiantamento()

        val boleto = BoletoVf.from(downPayment, installment)

        assertEquals(0.0, boleto.TaxaJurosMoraPercent)
        assertEquals(0.0, boleto.JurosValor)
        assertEquals(1000.0, boleto.ValorTitulo)
        assertEquals(1000.0, boleto.ValorTotal)
    }

    @Test
    fun `parcela vencida com taxa detalha juros e soma no valor total`() {
        // vencida ha 10 dias: a data de referencia do PIX e hoje+1, entao sao 11 dias de mora
        val installment = Installment(LocalDate.now().minusDays(10), 1000.0).also { it.InstallmentId = 1 }
        val downPayment = adiantamento()

        val boleto = BoletoVf.from(downPayment, installment, jurosMoraPercent = 0.1)

        assertEquals(0.1, boleto.TaxaJurosMoraPercent)
        assertEquals(11.0, boleto.JurosValor)
        assertEquals(1000.0, boleto.ValorTitulo)
        assertEquals(1011.0, boleto.ValorTotal)
    }

    @Test
    fun `parcela a vencer nao tem juros mesmo com taxa configurada`() {
        val installment = Installment(LocalDate.now().plusDays(5), 1000.0).also { it.InstallmentId = 1 }
        val downPayment = adiantamento()

        val boleto = BoletoVf.from(downPayment, installment, jurosMoraPercent = 0.1)

        assertEquals(0.0, boleto.JurosValor)
        assertEquals(1000.0, boleto.ValorTotal)
    }

    @Test
    fun `adiantamento sem parcelas usa o DocTotal como valor do titulo`() {
        val boleto = BoletoVf.from(adiantamento(), jurosMoraPercent = 0.1)

        assertEquals(0.0, boleto.JurosValor)
        assertEquals(1500.0, boleto.ValorTitulo)
        assertEquals(1500.0, boleto.ValorTotal)
    }

    @Test
    fun `o detalhamento de juros chega no json consumido pelo front`() {
        // a classe usa @JsonInclude(NON_EMPTY): garante que os campos numericos nao sao suprimidos
        val installment = Installment(LocalDate.now().minusDays(10), 1000.0).also { it.InstallmentId = 1 }
        val boleto = BoletoVf.from(adiantamento(), installment, jurosMoraPercent = 0.1)

        val json = ObjectMapper().registerKotlinModule().writeValueAsString(boleto)

        assertTrue(json.contains("\"JurosValor\":11.0"), json)
        assertTrue(json.contains("\"ValorTitulo\":1000.0"), json)
        assertTrue(json.contains("\"ValorTotal\":1011.0"), json)
        assertTrue(json.contains("\"TaxaJurosMoraPercent\":0.1"), json)
    }

    private fun adiantamento(): Document {
        return Document("C1", LocalDate.now().toString(), listOf(), "2").also {
            it.docEntry = 123
            it.docNum = "456"
            it.DocTotal = "1500.00"
            it.DocumentStatus = DocumentStatus.bost_Open
        }
    }
}
