package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.services.FazendaService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("fazenda")
class FazendaController(
        val fazendaService: FazendaService) {


    @GetMapping() 
    fun getFazendas() : Any{
        return fazendaService.get()
    }

    @GetMapping("{id}")
    fun getFazendaById(@PathVariable id : String) : OData{
        return fazendaService.get(Filter(mutableListOf(Predicate("Code",id, Condicao.EQUAL))))
    }


}