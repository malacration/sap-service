package br.andrew.sap.rovema.controllers

import br.andrew.sap.rovema.infrastructure.odata.*
import br.andrew.sap.rovema.services.FazendaService
import br.andrew.sap.rovema.services.RomaneioService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.IDN

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
        return fazendaService.get(Filter(listOf(Predicate("Code",id, Condicao.EQUAL))))
    }


}