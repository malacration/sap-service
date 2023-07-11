package br.andrew.sap.model.uzzipay

import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.Installment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class RequestQrCodeTests {

    @Test
    fun defragmentacaoDocEntryAndInstallId(){
        val installment = Installment(Date(), 100.00).also { it.InstallmentId = 666 }
        val document = Document("gabriel", "1", listOf(),"windson")
            .also {
                it.documentInstallments = listOf(installment)
                it.docEntry = 123
                it.docNum = "333"
                it.docObjectCode = "invoice"
            }

        val request = RequestQrCode(
            installment.createExternalIdentifier(document),
            ContaUzziPayPix().also { it.chavePix = "" },
            Type.EVP,
            100.00.toBigDecimal(),
            "2024-12-31",
            Payer(
                "01847004261",
                "Andrew",
                "andrewc3po@gmail.com",
                "Rua dos Bobos",
                "São Paulo",
                "SP",
                "12345678"),
            "1")

        Assertions.assertEquals(666,request.getInstallmentId())
        Assertions.assertEquals("333",request.docNum())
        Assertions.assertEquals("invoice",request.docType())
        Assertions.assertEquals(123,request.docEntry())
    }
}