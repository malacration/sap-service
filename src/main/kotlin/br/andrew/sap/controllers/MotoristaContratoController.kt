package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.services.MotoristaContratoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
    @RequestMapping("motorista-contrato")
class MotoristaContratoController(val motoristaContratoService : MotoristaContratoService) {

    @GetMapping()
    fun getRegistros(): OData {
        return motoristaContratoService.get()
    }

    @GetMapping("{id}")
    fun getByCodParceiro(@PathVariable id : String): OData {
        return motoristaContratoService.getById(id).tryGetValue()
    }
}