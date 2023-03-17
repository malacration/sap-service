package br.andrew.sap.rovema.controllers

import br.andrew.sap.rovema.infrastructure.odata.*
import br.andrew.sap.rovema.model.Fazenda
import br.andrew.sap.rovema.model.RegistroCompraInsumo
import br.andrew.sap.rovema.model.RomaneioPesagem
import br.andrew.sap.rovema.services.FazendaService
import br.andrew.sap.rovema.services.RegistroCompraInsumoService
import br.andrew.sap.rovema.services.RomaneioPesagemService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("romaneio-pesagem")
class RomaneioPesagemController(
        val romaneioService: RomaneioPesagemService,
        val fazendaService: FazendaService,
        val registroCompraInsumoService: RegistroCompraInsumoService) {


    @GetMapping()
    fun getRomaneio() : List<RomaneioPesagem> {
        return romaneioService
                .get(OrderBy(mapOf("DocEntry" to Order.DESC)))
                .tryGetValues<RomaneioPesagem>()
    }

    @GetMapping("{docNum}")
    fun getById(@PathVariable docNum : Int) : List<RomaneioPesagem>{
        return romaneioService
                .get(Filter(listOf(Predicate("DocNum",docNum, Condicao.EQUAL))))
                .tryGetValues<RomaneioPesagem>()
    }

    @GetMapping("contrato-fazenda")
    fun getByContrato() : List<RomaneioPesagem>{
        val pns = registroCompraInsumoService
                .get()
                .tryGetValues<RegistroCompraInsumo>()
                .map { it.U_CodParceiroNegocio }
        val predicate = Predicate("U_CodParceiro",pns,Condicao.IN)
        Filter(listOf())
        return romaneioService
                .get()
                .tryGetValues<RomaneioPesagem>()
    }


}