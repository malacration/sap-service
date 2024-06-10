package br.andrew.sap.controllers

import br.andrew.sap.model.SalePerson
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.SalesPersonsService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.rdstation.Event
import br.andrew.sap.services.rdstation.EventsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("rdstation")
class RdStationController(
    private val eventsService: EventsService,
    private val bussinesPartenerService: BusinessPartnersService,
    private val slService: SalesPersonsService,
    private val ordersService: OrdersService
    ) {

    @GetMapping("/pedido-venda/{id}")
    fun getByCodParceiro(@PathVariable id: String): Any {
        val documento: Document = ordersService.getById(id).tryGetValue()
        return getByCodParceiro(documento)
    }

    fun getByCodParceiro(documento: Document): Any {
        val businessPartner: BusinessPartner = bussinesPartenerService.getById("'${documento.CardCode}'").tryGetValue<BusinessPartner>()
        val salesPersonCode = documento.salesPersonCode
        val salesPersonCodeAll = slService.getAll<SalePerson>()
        val salePerson = salesPersonCodeAll.find { it.SalesEmployeeCode == salesPersonCode }
        if (salePerson == null) {
            return ResponseEntity.notFound()
        }
        return eventsService.conversion(documento, businessPartner, salePerson)
    }

}