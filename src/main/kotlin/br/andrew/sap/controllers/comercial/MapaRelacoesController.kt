package br.andrew.sap.controllers.comercial

import br.andrew.sap.model.comercial.MapaRelacoesResponse
import br.andrew.sap.model.comercial.MapaTipoDocumento
import br.andrew.sap.services.comercial.MapaRelacoesService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("mapa-relacoes")
class MapaRelacoesController(val service: MapaRelacoesService) {

    @GetMapping("/{tipo}/{docEntry}")
    fun mapa(@PathVariable tipo: String, @PathVariable docEntry: Int): MapaRelacoesResponse {
        return service.mapa(parseTipo(tipo), docEntry)
    }

    private fun parseTipo(tipo: String): MapaTipoDocumento {
        return when (tipo.lowercase()) {
            "cotacao" -> MapaTipoDocumento.COTACAO
            "pedido" -> MapaTipoDocumento.PEDIDO
            "nota-fiscal" -> MapaTipoDocumento.NOTA_FISCAL
            "adiantamento" -> MapaTipoDocumento.ADIANTAMENTO
            "devolucao" -> MapaTipoDocumento.DEVOLUCAO
            "contrato" -> MapaTipoDocumento.CONTRATO
            else -> throw Exception("Tipo de documento invalido: $tipo (use cotacao, pedido, nota-fiscal, adiantamento, devolucao ou contrato)")
        }
    }
}
