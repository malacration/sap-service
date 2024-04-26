package br.andrew.sap.controllers


import br.andrew.sap.controllers.documents.InvoicesController
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.Version
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.*
import br.andrew.sap.services.bank.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class IndexController(val version : Version,
                      val service : BusinessPartnersService){

    val logger = LoggerFactory.getLogger(IndexController::class.java)

    @GetMapping("/")
    fun index() : Version{
        return version
    }
}
