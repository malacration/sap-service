package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.services.FazendaService
import br.andrew.sap.services.OrdemCarregamentoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("ordem-carregamento")
class OrdemCarregamentoController(
        val ordemCarregamentoService: OrdemCarregamentoService) {

    @GetMapping()
    fun getOrdemCarregamento() : OData{
        return ordemCarregamentoService.get()
    }
}