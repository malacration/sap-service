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

    @GetMapping("/key/{hashcode}")
    fun getHashUpdat(@PathVariable hashcode : String): OData {
        //TODO fazer metodo que busca hash do cliente para verificar atualizacao
        return service.getById("'$hashcode'")
    }

    @PostMapping("/key/{hashcode}")
    fun setHashUpdat(@RequestBody bp : BusinessPartner): OData? {
        //TODO fazer metodo que busca hash do cliente para verificar atualizacao

        bp.clearDataNotAllowUpdated()
//        var b = bp.let { BusinessPartner().also { it.setAddresses(bp.getAddresses().filter { it.RowNum == null }) } }
//        b.cardCode = bp.cardCode
//        service.update(b,"'${bp.cardCode}'")
////bp
        return service.update(bp,"'${bp.cardCode}'")
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