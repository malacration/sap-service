package br.andrew.sap.controllers.romaneio

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.enums.TipoContratoFazenda
import br.andrew.sap.model.romaneio.RomaneioPesagem
import br.andrew.sap.services.FazendaService
import br.andrew.sap.services.RegistroCompraInsumoService
import br.andrew.sap.services.romaneio.RomaneioPesagemService
import br.andrew.sap.services.romaneio.RomaneioPesagemServiceSaida
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("romaneio-pesagem")
class RomaneioPesagemController(
        val romaneioService: RomaneioPesagemService,
        val romaneioServiceSaida: RomaneioPesagemServiceSaida,
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
                .get(Filter(mutableListOf(Predicate("DocNum",docNum, Condicao.EQUAL))))
                .tryGetValues<RomaneioPesagem>()
    }

    @GetMapping("contrato-fazenda")
    fun getByContrato(page: Pageable,
                      @RequestParam("bp") filial: String?,
                      @RequestParam("nfNum") numero: Int?,
                      @RequestParam("tipo-contrato") contratofazenda: TipoContratoFazenda) : Page<RomaneioPesagem> {
        val parametros = mutableListOf(
            Predicate("bp", filial ?: "all", Condicao.EQUAL),
            Predicate("nfNum", numero ?: -666, Condicao.EQUAL)
        )

        return if (contratofazenda == TipoContratoFazenda.ENTRADA) {
            romaneioService.romaneisoSemEntrada(page, Filter(parametros))
        } else {
            romaneioServiceSaida.romaneisoSemSaida(page, Filter(parametros))
        }
    }

}