package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("pix")
class PixController(
    val transactionsPixService: TransactionsPixService,
    val uzziPayEnvrioment: UzziPayEnvrioment,
    val invoiceService: InvoiceService){

    @GetMapping()
    fun test() : Any?{
        return "ok"
    }

    @GetMapping("transaction/{id}")
    fun consultaTransactionQrCode(@PathVariable id : String): Transaction {
        return transactionsPixService.getBy(id)
    }


    @GetMapping("transaction/{id}/conta/{cnpj}/baixa")
    fun verificaPixEhBaixa(@PathVariable id : String, @PathVariable cnpj : String): Any {
        val conta : ContaUzziPayPix = uzziPayEnvrioment.getContaByCnpj(cnpj)
        val transaction = transactionsPixService.getBy(id,conta)
        return invoiceService.baixaPixBy(transaction,conta)
    }
}