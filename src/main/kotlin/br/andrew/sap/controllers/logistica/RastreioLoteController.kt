package br.andrew.sap.controllers.logistica

import br.andrew.sap.model.comercial.MapaRelacoesResponse
import br.andrew.sap.services.logistica.RastreioLoteService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Um nivel do rastreio de lote (o lote, a OP que o gerou e os lotes que ela consumiu), no formato do mapa
 * de relacoes. `item` vem no clique de um lote do mapa; na busca digitada fica vazio. Fica sob
 * /ordens-producao para valer a mesma regra de acesso do detalhe da OP no rules.yml.
 */
@RestController
@RequestMapping("ordens-producao")
class RastreioLoteController(val service: RastreioLoteService) {

    @GetMapping("rastreio-lote")
    fun rastrear(@RequestParam lote: String, @RequestParam(required = false) item: String?): MapaRelacoesResponse =
        service.nivel(lote, item)

    //para onde o lote foi: OPs que o consumiram, lotes que elas geraram e vendas com o cliente
    @GetMapping("rastreio-lote/destino")
    fun destino(@RequestParam lote: String, @RequestParam item: String): MapaRelacoesResponse =
        service.destino(lote, item)
}
