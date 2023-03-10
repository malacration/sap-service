package br.andrew.sap.rovema.controllers

import br.andrew.sap.rovema.infrastructure.odata.Order
import br.andrew.sap.rovema.infrastructure.odata.OrderBy
import br.andrew.sap.rovema.model.RomaneioPesagem
import br.andrew.sap.rovema.services.RomaneioEntradaInsumoService
import br.andrew.sap.rovema.services.RomaneioService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("romaneio-entrada-insumo")
class RomaneioEntradaInsumoController(
        val romaneioEntradaServie: RomaneioEntradaInsumoService,
        val romaneioService: RomaneioService) {


    @GetMapping()
    fun getRomaneio() : Any{
        return romaneioEntradaServie.get()
    }

    @GetMapping("last")
    fun getLast() : Any{
        return romaneioEntradaServie.get(OrderBy(mapOf("DocNum" to Order.DESC)))
    }

    @GetMapping("/save")
    fun save() : Any{
        val entry = romaneioService.get().tryGetValues<RomaneioPesagem>().get(0).getRomaneioEntradaInsumo()
        return romaneioEntradaServie.save(entry)
    }


}