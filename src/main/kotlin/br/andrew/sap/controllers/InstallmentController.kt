package br.andrew.sap.controllers


import br.andrew.sap.services.*
import br.andrew.sap.services.bank.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
@RequestMapping("installment")
class InstallmentController(val service: IncomingPaymentService){

    @GetMapping("/{docentry}/paid")
    fun findInstallIsPayable(@PathVariable docentry : Int) : Any{
        return service.installPayment(docentry)
    }
}
