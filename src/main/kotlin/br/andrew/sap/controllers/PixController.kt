package br.andrew.sap.controllers

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.dto.PixGeradoResponse
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.PixDocType
import br.andrew.sap.model.sap.documents.matches
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.uzzipay.PixPaymentVerificationService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("pix")
class PixController(
    val transactionsPixService: TransactionsPixService,
    val invoiceService: InvoiceService,
    val pixPaymentVerificationService: PixPaymentVerificationService,
    @Value("\${pix.juros.mora.percent:0}") val jurosMoraPercent: Double){

    @GetMapping()
    fun test() : Any?{
        return "ok"
    }

    @GetMapping("gerar-chave/docType/{pixDocType}/docEntry/{docEntry}/parcela/{parcela}")
    fun gerarChave(
        @PathVariable pixDocType : PixDocType,
        @PathVariable docEntry : Int,
        @PathVariable parcela : Int,
        @RequestParam("juros", defaultValue = "true") juros: Boolean = true,
        auth : Authentication
    ): List<PixGeradoResponse> {
        if(auth !is User)
            throw Exception("Não foi possivel fazer a conversão de auth para User")
        if(!auth.isAllCreatePix(juros))
            throw Exception("Não é permitido criar pix!")
        val jurosPercent = if(juros) jurosMoraPercent else 0.0
        if (pixDocType.matches(DocumentTypes.oInvoices)) {
            val invoice = invoiceService.getById(docEntry).tryGetValue<Invoice>()
            return invoiceService.createPix(invoice,parcela,jurosPercent).map { installment ->
                PixGeradoResponse(installment, jurosPercent)
            }
        } else {
            throw Exception("Tipo de documento não permitido para gerar chave pix")
        }
    }

    @GetMapping("checar-chave/docType/{pixDocType}/docEntry/{docEntry}/parcela/{parcela}")
    fun checkChave(
        @PathVariable pixDocType : PixDocType,
        @PathVariable docEntry : Int,
        @PathVariable parcela : Int,
    ): Transaction {
        if (pixDocType.matches(DocumentTypes.oInvoices)) {
            val invoice = invoiceService.getById(docEntry)
                .tryGetValue<Invoice>()
            val parcelaPix = invoice.documentInstallments?.firstOrNull { it.InstallmentId == parcela }
            return pixPaymentVerificationService.verificaPixEhBaixa(
                invoice,
                parcelaPix ?: throw Exception("Referencia a Parcela não encontrada")
            )
        } else {
            throw Exception("Tipo de documento não permitido para gerar chave pix")
        }
    }

    @GetMapping("transaction/{id}")
    fun consultaTransactionQrCode(@PathVariable id : String): Transaction {
        return transactionsPixService.getBy(id)
    }

    @GetMapping("transaction/{id}/conta/{cnpj}/baixa")
    fun verificaPixEhBaixa(@PathVariable id : String, @PathVariable cnpj : String): Transaction {
        //TODO mudar isso depois.
        throw Exception("Erro, esse parametro agora utiliza ID da filial e nao cnpj")
    }
}
