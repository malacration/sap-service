package br.andrew.sap.controllers

import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("qr")
class DynamicQrCodeController(val dynamicQrCodeService: DynamicPixQrCodeService){

    @GetMapping()
    fun test() : Any?{
        return dynamicQrCodeService.generateQrCode("867816ce-1862-431c-b551-7784295094b7","1")
    }
}