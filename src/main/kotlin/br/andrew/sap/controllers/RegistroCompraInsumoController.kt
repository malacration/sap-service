package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.services.RegistroCompraInsumoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("reg-compra-insumo")
class RegistroCompraInsumoController(val registroCompraService : RegistroCompraInsumoService) {

    @GetMapping()
    fun getRegistros(): OData {
        return registroCompraService.get()
    }

    @GetMapping("pn/{codPn}")
    fun getByCodParceiro(@PathVariable codPn : String): OData {
        return registroCompraService.getByPn(codPn)
    }
}