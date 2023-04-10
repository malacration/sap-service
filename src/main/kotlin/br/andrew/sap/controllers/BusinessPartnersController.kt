package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.BusinessPartner
import br.andrew.sap.model.PaymentMethod
import br.andrew.sap.services.BusinessPartnersService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("business-partners")
class BusinessPartnersController(val service : BusinessPartnersService) {


    @GetMapping()
    fun get(): OData {
        return service.get()
    }

    @GetMapping("update")
    fun update(): OData? {
        val bp : BusinessPartner = BusinessPartner("FOR0000032")
                .also { it.setBPPaymentMethods(listOf(PaymentMethod("BB-PG-BOL-1199")))  }
        return service.update(bp,"'FOR0000032'")
    }
}