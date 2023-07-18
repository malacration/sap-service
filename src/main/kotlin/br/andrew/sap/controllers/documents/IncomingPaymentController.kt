package br.andrew.sap.controllers.documents

import br.andrew.sap.services.bank.IncomingPaymentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("incoming-payments")
class IncomingPaymentController(val incomingService: IncomingPaymentService) {

    @GetMapping("{docEntry}")
    fun get(@PathVariable docEntry : Int): Any {
        return incomingService.getById(docEntry)
    }
}