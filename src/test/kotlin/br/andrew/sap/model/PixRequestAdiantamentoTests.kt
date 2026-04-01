package br.andrew.sap.model

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import br.andrew.sap.model.uzzipay.RetonroPixQrCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

class PixRequestAdiantamentoTests {

    @BeforeEach
    fun resetStaticContas() {
        PixRequestAdiantamento.clearContasConfiguradas()
    }

    @Test
    fun getImmediateRequestSemContasConfiguradasLancaErro() {
        val request = PixRequestAdiantamento("C-001", BigDecimal("100.00"), 1)

        val erro = assertThrows<Exception> { request.getImmediateRequest() }
        assertTrue((erro.message ?: "").contains("Nenhuma conta configurada"))
    }

    @Test
    fun construtorComDocEntrySemDocTypeLancaErro() {
        val erro = assertThrows<Exception> {
            PixRequestAdiantamento("C-001", BigDecimal("100.00"), 1, docEntry = 10)
        }
        assertTrue((erro.message ?: "").contains("docEntry e documentTypes devem ser informados juntos"))
    }

    @Test
    fun construtorComDocTypeSemDocEntryLancaErro() {
        val erro = assertThrows<Exception> {
            PixRequestAdiantamento("C-001", BigDecimal("100.00"), 1, documentTypes = DocumentTypes.oOrders)
        }
        assertTrue((erro.message ?: "").contains("docEntry e documentTypes devem ser informados juntos"))
    }


    @Test
    fun getImmediateRequestSelecionaContaDaFilialEMapeiaCampos() {
        val conta = newConta(idFilial = 3, cnpj = "12345678000190", chavePix = "PIX-OK")
        val env = UzziPayEnvrioment().also { it.contas = listOf(conta, newConta(2, "00999999000199", "PIX-OUTRA")) }
        PixRequestAdiantamento.setUzziPayEnvrioment(env)
        val request = PixRequestAdiantamento(
            cardCode = "C-001",
            valor = BigDecimal("150.55"),
            idFilial = 3,
            docEntry = 987,
            documentTypes = DocumentTypes.oInvoices
        )

        val immediate = request.getImmediateRequest()

        assertEquals("PIX-OK", immediate.key)
        assertEquals("12345678000190", immediate.getCnpj())
        assertEquals("150.55", immediate.getAmount())
        assertEquals(987, immediate.docEntry())
        assertEquals("oInvoices", immediate.docType())
        assertEquals(0, immediate.getInstallmentId())
        assertTrue(immediate.externalIdentifier.contains("Num987-Entry987-ins:0-oInvoices"))
    }

    @Test
    fun getImmediateRequestComAdiantamentoUsaDocNumDocEntryEParcelaGerada() {
        val conta = newConta(idFilial = 3, cnpj = "12345678000190", chavePix = "PIX-OK")
        PixRequestAdiantamento.setUzziPayEnvrioment(UzziPayEnvrioment().also { it.contas = listOf(conta) })
        val request = PixRequestAdiantamento(
            cardCode = "C-001",
            valor = BigDecimal("150.55"),
            idFilial = 3
        )
        val adiantamento = DownPayment("C-001", LocalDate.now().toString(), listOf(), "3").also {
            it.docEntry = 321
            it.docNum = "654"
            it.docObjectCode = DocumentTypes.oDownPayments
            it.documentInstallments = listOf(Installment(LocalDate.now().plusDays(1), 150.55).also { parcela ->
                parcela.InstallmentId = 1
            })
        }

        val immediate = request.getImmediateRequest(adiantamento)

        assertEquals(321, immediate.docEntry())
        assertEquals("654", immediate.docNum())
        assertEquals("oDownPayments", immediate.docType())
        assertEquals(1, immediate.getInstallmentId())
        assertTrue(immediate.externalIdentifier.contains("Num654-Entry321-ins:1-oDownPayments"))
    }

    @Test
    fun setPixImmediateDiretoNoInstallmentPreencheCamposPix() {
        val conta = newConta(idFilial = 3, cnpj = "12345678000190", chavePix = "PIX-OK")
        PixRequestAdiantamento.setUzziPayEnvrioment(UzziPayEnvrioment().also { it.contas = listOf(conta) })
        val request = PixRequestAdiantamento(
            cardCode = "C-001",
            valor = BigDecimal("150.55"),
            idFilial = 3
        )
        val installment = Installment(LocalDate.now(), 150.55).also {
            it.InstallmentId = 0
        }
        val immediate = request.getImmediateRequest()
        val retorno = DataRetonroPixQrCode(RetonroPixQrCode("pix-copia-cola", "https://pix", "ref-123", null))

        installment.setPix(immediate, retorno)

        assertEquals("pix-copia-cola", installment.U_QrCodePix)
        assertEquals("https://pix", installment.U_pix_link)
        assertEquals("ref-123", installment.U_pix_reference)
        assertTrue(!installment.U_pix_due_date.isNullOrBlank())
        assertTrue(!installment.U_pix_proxima_consulta_em.isNullOrBlank())
        assertTrue(!installment.U_pix_consultar_ate.isNullOrBlank())
    }

    private fun newConta(idFilial: Int, cnpj: String, chavePix: String): ContaUzziPayPix {
        return ContaUzziPayPix().also {
            it.idFilial = idFilial
            it.cnpj = cnpj
            it.chavePix = chavePix
            it.privateKey = "private-key"
            it.consulta = "consulta"
            it.contaContabilBanco = "conta"
        }
    }
}
