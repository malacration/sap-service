package br.andrew.sap.rovema.controllers

import br.andrew.sap.rovema.infrastructure.odata.Order
import br.andrew.sap.rovema.infrastructure.odata.OrderBy
import br.andrew.sap.rovema.model.Fazenda
import br.andrew.sap.rovema.model.romaneio.Filho
import br.andrew.sap.rovema.model.romaneio.RomaneioEntradaInsumoMinimo
import br.andrew.sap.rovema.services.FazendaService
import br.andrew.sap.rovema.services.RomaneioEntradaInsumoService
import br.andrew.sap.rovema.services.RomaneioService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Date

@RestController
@RequestMapping("romaneio-entrada-insumo")
class RomaneioEntradaInsumoController(
        val romaneioEntradaServie: RomaneioEntradaInsumoService,
        val romaneioService: RomaneioService,
        val fazendaController: FazendaController) {


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
       return null;
    }

    @GetMapping("/update")
    fun update() : Any?{
        val fazenda = fazendaController.getFazendaById("RMA").tryGetValues<Fazenda>().first()
        val romaneio = RomaneioEntradaInsumoMinimo(
                Date(),
                666,
                "402.100",
                500.0,
                5000.0,
                300.0,
                "ABC-1234",
        ).also { it.setFazenda(fazenda) }
//                .also {
//                    it.DocEntry = 286
////                    it.PECU_REGA.forEach{linha -> linha.LineId = 1; linha.DocEntry = it.DocEntry}
//                }
        return romaneioEntradaServie.update(romaneio, romaneio.DocEntry.toString())
    }


}