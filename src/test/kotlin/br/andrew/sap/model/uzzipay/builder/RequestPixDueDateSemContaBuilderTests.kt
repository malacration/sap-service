package br.andrew.sap.model.uzzipay.builder

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.CpfCnpj
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class RequestPixDueDateSemContaBuilderTests {

    @BeforeEach
    fun resetStaticContas() {
        RequestPixDueDateSemContaBuilder.clearContasConfiguradas()
    }

    @Order(1)
    @Test
    fun buildSemContasConfiguradasLancaErro() {
        val builder = RequestPixDueDateSemContaBuilder(
            newBusinessPartner(),
            newDocument(listOf(newInstallment(1)))
        )

        val erro = assertThrows<Exception> { builder.build() }
        assertTrue((erro.message ?: "").contains("Nenhuma conta configurada"))
    }

    @Order(2)
    @Test
    fun comContasSemCnpjCorrespondenteLancaErro() {
        val builder = RequestPixDueDateSemContaBuilder(
            newBusinessPartner(),
            newDocument(listOf(newInstallment(1)))
        )
        val contas = listOf(newConta("00999999000199", "PIX-1"))

        val erro = assertThrows<Exception> { builder.comContas(contas) }
        assertTrue((erro.message ?: "").contains("Conta não encontrada para a filial 1"))
    }

    @Order(3)
    @Test
    fun buildSelecionaContaPorCnpjEConstruiParcelas() {
        val contaSelecionada = newConta("12345678000190", "PIX-OK")
        val outras = newConta("00999999000199", "PIX-OUTRA")
        val env = UzziPayEnvrioment().also { it.contas = listOf(outras, contaSelecionada) }
        RequestPixDueDateSemContaBuilder.setUzziPayEnvrioment(env)

        val builder = RequestPixDueDateSemContaBuilder(
            newBusinessPartner(),
            newDocument(listOf(newInstallment(1), newInstallment(2)))
        )

        val requests = builder.build()
        assertEquals(2, requests.size)
        requests.forEach { request ->
            assertEquals("PIX-OK", request.key)
            assertEquals("12345678000190", request.getCnpj())
            assertTrue(request.externalIdentifier.contains("Num456-Entry123-ins:"))
        }
    }

    @Order(4)
    @Test
    fun buildComParcelasFiltraSomenteInformadas() {
        val contaSelecionada = newConta("12345678000190", "PIX-OK")
        val env = UzziPayEnvrioment().also { it.contas = listOf(contaSelecionada) }
        RequestPixDueDateSemContaBuilder.setUzziPayEnvrioment(env)

        val builder = RequestPixDueDateSemContaBuilder(
            newBusinessPartner(),
            newDocument(listOf(newInstallment(1), newInstallment(2))),
            parcela = listOf(2)
        )

        val requests = builder.build()
        assertEquals(1, requests.size)
        assertEquals(2, requests.first().getInstallmentId())
    }

    private fun newBusinessPartner(): BusinessPartner {
        return BusinessPartner().also { bp ->
            bp.cardName = "Cliente Teste"
            bp.emailAddress = "cliente@teste.com"
            bp.setCpfCnpj(CpfCnpj("12345678901"))
            bp.setAddresses(
                listOf(
                    Address().also {
                        it.addressName = "Rua A"
                        it.County = "Cuiaba"
                        it.State = "MT"
                        it.ZipCode = "78000000"
                    }
                )
            )
        }
    }

    private fun newBussinessPlace(cnpj: String): BussinessPlace {
        return BussinessPlace().also { it.FederalTaxID = cnpj }
    }

    private fun newDocument(installments: List<Installment>): Document {
        return Document("C1", "2026-01-30", listOf(), "1").also {
            it.documentInstallments = installments
            it.docEntry = 123
            it.docNum = "456"
            it.docObjectCode = DocumentTypes.oInvoices
        }
    }

    private fun newInstallment(id: Int): Installment {
        return Installment(LocalDate.now().plusDays(5), 100.0).also { it.InstallmentId = id }
    }

    private fun newConta(cnpj: String, chavePix: String): ContaUzziPayPix {
        return ContaUzziPayPix().also {
            it.cnpj = cnpj
            it.idFilial = if(chavePix == "PIX-OK") 1 else 2
            it.chavePix = chavePix
            it.privateKey = "key"
            it.consulta = "consulta"
            it.contaContabilBanco = "contabil"
        }
    }
}
