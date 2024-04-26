package br.andrew.sap.controllers.romaneio

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.services.RegistroVendaInsumoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("reg-venda-insumo")
class RegistroVendaInsumoController(val registroVendaService : RegistroVendaInsumoService) {

    @GetMapping()
    fun getRegistros(): OData {
        return registroVendaService.get()
    }

    @GetMapping("pn/{codPn}")
    fun getByCodParceiro(@PathVariable codPn : String): OData {
        return registroVendaService.getByPn(codPn)
    }
}