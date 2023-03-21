package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.Fazenda
import br.andrew.sap.model.RegistroCompraInsumo
import br.andrew.sap.model.RomaneioPesagem
import br.andrew.sap.services.FazendaService
import br.andrew.sap.services.RegistroCompraInsumoService
import br.andrew.sap.services.RomaneioPesagemService
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
        return romaneioService
                .get(Filter(listOf(predicate)))
                .tryGetValues<RomaneioPesagem>()
    }


}