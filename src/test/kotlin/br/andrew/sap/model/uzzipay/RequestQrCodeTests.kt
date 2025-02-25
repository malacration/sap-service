package br.andrew.sap.model.uzzipay

import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class RequestQrCodeTests {

    val player = Payer(
        "123545546",
        "Andrew",
        "windson@gmail.com",
        "Rua dos Bobos",
        "São Paulo",
        "SP",
        "12345678")
    val installment = Installment(LocalDate.now(), 100.00).also { it.InstallmentId = 666 }
    val document = Document("gabriel", "1", listOf(),"windson")
        .also {
            it.documentInstallments = listOf(installment)
            it.docEntry = 123
            it.docNum = "333"
            it.docObjectCode = DocumentTypes.oInvoices
        }
    @Test
    fun defragmentacaoDocEntryAndInstallId(){
        val request = RequestPixDueDate(
            installment.createExternalIdentifier(document),
            ContaUzziPayPix().also { it.chavePix = "" },
            100.00.toBigDecimal(),
            "2924-12-31",
            player,
            "1")

        Assertions.assertEquals(666,request.getInstallmentId())
        Assertions.assertEquals("333",request.docNum())
        Assertions.assertEquals("oInvoices",request.docType())
        Assertions.assertEquals(123,request.docEntry())
        Assertions.assertTrue(
            request.externalIdentifier.contains("Num333-Entry123-ins:666-oInvoices"))
        Assertions.assertEquals("", Installment.reverseExternalIdentifier(""))
    }

    @Test
    fun dataHojeRetornaUmDiaMais(){
        val request = RequestPixDueDate(
            installment.createExternalIdentifier(document),
            ContaUzziPayPix().also { it.chavePix = "" },
            100.00.toBigDecimal(),
            LocalDate.now(),
            player,
            "1")
        val saida = SimpleDateFormat("yyyy-MM-dd")
            .format(Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
        Assertions.assertEquals(saida,request.getDueDate())
    }

    @Test
    fun dataHoraVencidoDeveDarErro(){
        val erro = assertThrows<Exception> {
            RequestPixDueDate(
                installment.createExternalIdentifier(document),
                ContaUzziPayPix().also { it.chavePix = "" },
                100.00.toBigDecimal(),
                LocalDate.now().plusDays(-10),
                player,
                "1")
        }
        Assertions.assertTrue((erro.message ?: "").contains("vencimento não pode ser menor que a data atual"))
    }
}