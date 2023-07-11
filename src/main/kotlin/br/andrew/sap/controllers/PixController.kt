package br.andrew.sap.controllers

import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("pix")
class PixController(
    val dynamicQrCodeService: DynamicPixQrCodeService,
    val transactionsPixService: TransactionsPixService){

    @GetMapping()
    fun test() : Any?{
        return "ok"
    }



    @GetMapping("transaction/{id}")
    fun consultaTransactionQrCode(@PathVariable id : String): Any? {
        return transactionsPixService.get(id)
    }
}