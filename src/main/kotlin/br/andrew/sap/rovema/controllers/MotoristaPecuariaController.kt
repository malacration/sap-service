package br.andrew.sap.rovema.controllers

import br.andrew.sap.rovema.infrastructure.odata.Condicao
import br.andrew.sap.rovema.infrastructure.odata.Filter
import br.andrew.sap.rovema.infrastructure.odata.OData
import br.andrew.sap.rovema.infrastructure.odata.Predicate
import br.andrew.sap.rovema.services.MotoristaPecuariaService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
    @RequestMapping("motorista-pecuaria")
class MotoristaPecuariaController(val motoristaPecuariaService : MotoristaPecuariaService) {

    @GetMapping()
    fun getRegistros(): OData {
        return motoristaPecuariaService.get()
    }

    @GetMapping("cnh/{id}")
    fun getByCodParceiro(@PathVariable id : String): OData {
        return motoristaPecuariaService.getByCnh(id)
    }
}