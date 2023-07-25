package br.andrew.sap.controllers.pricing

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.pricing.PriceListsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("comissao")
class ComissaoController(val service: ComissaoService) {


    @GetMapping()
    fun get() : Any{
        return service.get()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : Int) : Any{
        return service.get(id)
    }

}