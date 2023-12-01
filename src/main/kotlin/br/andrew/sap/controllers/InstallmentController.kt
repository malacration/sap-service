package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.DocEntry
import br.andrew.sap.model.Version
import br.andrew.sap.services.*
import br.andrew.sap.services.bank.*
import org.springframework.http.RequestEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.thymeleaf.TemplateEngine
import java.util.*


@RestController
class InstallmentController(val service: IncomingPaymentService){

    @GetMapping("/{docEntry}")
    fun findInstallIsPayable(@PathVariable docentry : Int) : Any{
        return service.installPayment(docentry)
    }
}
