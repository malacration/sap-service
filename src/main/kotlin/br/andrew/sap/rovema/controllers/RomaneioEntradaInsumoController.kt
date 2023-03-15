package br.andrew.sap.rovema.controllers

import br.andrew.sap.rovema.infrastructure.odata.OData
import br.andrew.sap.rovema.infrastructure.odata.Order
import br.andrew.sap.rovema.infrastructure.odata.OrderBy
import br.andrew.sap.rovema.model.Fazenda
import br.andrew.sap.rovema.model.romaneio.RomaneioEntradaInsumoMin
import br.andrew.sap.rovema.model.romaneio.TipoAnalise
import br.andrew.sap.rovema.services.TipoAnaliseService
import br.andrew.sap.rovema.services.RomaneioEntradaInsumoService
import br.andrew.sap.rovema.services.RomaneioService
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.RequestEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.Date

@RestController
@RequestMapping("romaneio-entrada-insumo")
class RomaneioEntradaInsumoController(
        val romaneioEntradaServie: RomaneioEntradaInsumoService,
        val romaneioService: RomaneioService,
        val fazendaController: FazendaController,
        val romaneioAnaliseService: TipoAnaliseService,
        val restTemplate : RestTemplate) {


    @GetMapping()
    fun getRomaneio() : Any{
        return romaneioEntradaServie.get()
    }

    @GetMapping("last")
    fun getLast() : Any{
        return romaneioEntradaServie.get(OrderBy(mapOf("DocNum" to Order.DESC)))
    }

    @GetMapping("/save")
    fun save() : Any?{
        val fazenda = fazendaController.getFazendaById("RMA").tryGetValues<Fazenda>().first()
        val romaneio = RomaneioEntradaInsumoMin(
                Date(),
                666,
                500.0,
                5000.0,
                300.0,
                "ABC-1234",
        ).also {
            it.setFazenda(fazenda);
            it.setMotorista();
            it.setResponsavel();
            it.setTransportadora();
            it.setSafra()
            it.setRegistroCompra()
        }
        return romaneioEntradaServie.save(romaneio)
    }

    @GetMapping("/update")
    fun update() : Any?{
        val fazenda = fazendaController.getFazendaById("RMA").tryGetValues<Fazenda>().first()
        val romaneio = RomaneioEntradaInsumoMin(
                Date(),
                666,
                500.0,
                5000.0,
                300.0,
                "ABC-1234",
        ).also {
            it.setFazenda(fazenda);
            it.setMotorista();
            it.setResponsavel();
            it.setTransportadora();
            it.setSafra()
            it.setRegistroCompra()
            it.DocEntry = 286
        }
        return romaneioEntradaServie.update(romaneio, romaneio.DocEntry.toString())
    }

    @GetMapping("/analise")
    fun analise() : Any?{
        val fazenda = fazendaController.getFazendaById("RMA").tryGetValues<Fazenda>().first()
        val romaneio = RomaneioEntradaInsumoMin(
                Date(),
                666,
                500.0,
                5000.0,
                300.0,
                "ABC-1234",
        ).also {
            it.setFazenda(fazenda);
            it.setMotorista();
            it.setResponsavel();
            it.setTransportadora();
            it.setSafra()
            it.setRegistroCompra()
        }
        return romaneioEntradaServie.save(romaneio)
    }




}