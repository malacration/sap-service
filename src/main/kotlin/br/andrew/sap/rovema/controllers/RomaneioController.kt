package br.andrew.sap.rovema.controllers

import br.andrew.sap.rovema.infrastructure.odata.Order
import br.andrew.sap.rovema.infrastructure.odata.OrderBy
import br.andrew.sap.rovema.services.RomaneioService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("romaneio")
class RomaneioController(
        val romaneioService: RomaneioService) {


    @GetMapping()
    fun getRomaneio() : Any{
        return romaneioService.get(OrderBy(mapOf("DocEntry" to Order.DESC)))
    }


}