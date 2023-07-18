package br.andrew.sap.controllers.documents

import br.andrew.sap.services.bank.VendorPaymentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("vendor-payment")
class VendorPaymentController(val vendorPaymentService: VendorPaymentService) {


    @GetMapping("{docEntry}")
    fun get(@PathVariable docEntry : Int): Any {
        return vendorPaymentService.getById(docEntry)
    }
}