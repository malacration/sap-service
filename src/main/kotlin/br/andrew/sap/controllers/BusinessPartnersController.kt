package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.partner.*
import br.andrew.sap.services.BusinessPartnersService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("business-partners")
class BusinessPartnersController(val service : BusinessPartnersService) {


    @GetMapping()
    fun get(): OData {
        return service.get()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String): OData {
        return service.getById("'$id'")
    }

    @PostMapping("")
    fun salvar(@RequestBody bp : Cliente): BusinessPartner {
        return service.save(bp.getBusinessPartner()).tryGetValue()
    }

    @PostMapping("save")
    fun save(@RequestBody bp : Cliente): BusinessPartner {
        return salvar(bp)
    }
}