package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.dto.PixGeradoResponse
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.PixDocType
import br.andrew.sap.model.sap.documents.matches
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.springframework.aot.hint.TypeReference.listOf
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("pix")
class PixController(
    val transactionsPixService: TransactionsPixService,
    val uzziPayEnvrioment: UzziPayEnvrioment,
    val invoiceService: InvoiceService,
    val businesPlaceService : BussinessPlaceService,
    @Value("\${pix.juros.mora.percent:0}") val jurosMoraPercent: Double){

    @GetMapping()
    fun test() : Any?{
        return "ok"
    }

    @GetMapping("gerar-chave/docType/{pixDocType}/docEntry/{docEntry}/parcela/{parcela}")
    fun gerarChave(
        @PathVariable pixDocType : PixDocType,
        @PathVariable docEntry : Int,
        @PathVariable parcela : Int
    ): List<PixGeradoResponse> {
        if (pixDocType.matches(DocumentTypes.oInvoices)) {
            val invoice = invoiceService.getById(docEntry).tryGetValue<Invoice>()
            return invoiceService.createPix(invoice,parcela,jurosMoraPercent).map { installment ->
                PixGeradoResponse(installment, jurosMoraPercent)
            }
        } else {
            throw Exception("Tipo de documento não permitido para gerar chave pix")
        }
    }

    @GetMapping("checar-chave/docType/{pixDocType}/docEntry/{docEntry}/parcela/{parcela}")
    fun checkChave(
        @PathVariable pixDocType : PixDocType,
        @PathVariable docEntry : Int,
        @PathVariable parcela : Int
    ): Transaction {
        if (pixDocType.matches(DocumentTypes.oInvoices)) {
            val invoice = invoiceService.getById(docEntry)
                .tryGetValue<Invoice>()
            val parcelaPix = invoice.documentInstallments?.firstOrNull { it.InstallmentId == parcela }
            val bp = businesPlaceService.getById(invoice.getBPL_IDAssignedToInvoice())
                .tryGetValue<BussinessPlace>()
            val conta : ContaUzziPayPix = uzziPayEnvrioment.getContaByCnpj(bp)
            val transaction = transactionsPixService
                .getBy(parcelaPix?.U_pix_reference?: throw Exception("Referencia a Parcela não encontrada") ,conta)
            invoiceService.baixaPixBy(transaction,conta)
            return transaction
        } else {
            throw Exception("Tipo de documento não permitido para gerar chave pix")
        }
    }

    @GetMapping("transaction/{id}")
    fun consultaTransactionQrCode(@PathVariable id : String): Transaction {
        return transactionsPixService.getBy(id)
    }

    @GetMapping("transaction/{id}/conta/{cnpj}/baixa")
    fun verificaPixEhBaixa(@PathVariable id : String, @PathVariable cnpj : String): Any {
        val conta : ContaUzziPayPix = uzziPayEnvrioment.getContaByCnpj(cnpj)
        val transaction = transactionsPixService.getBy(id,conta)
//        return invoiceService.baixaPixBy(transaction,conta)
        return listOf()
    }
}
