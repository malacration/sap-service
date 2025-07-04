package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.PainelIntegradoVendas
import br.andrew.sap.services.PainelIntegradoVendasService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("painel")
class PainelIntegradoVendasController(
    private val painelIntegradoVendasServices: PainelIntegradoVendasService
) {
    @GetMapping("pedidos")
    fun searchPedidos(
        @RequestParam("dataInicial") dataInicial: String,
        @RequestParam("dataFinal")   dataFinal:   String,
        @RequestParam("filial") filial: String,
        @RequestParam("cliente", required = false) cliente: String?,
        @RequestParam("item", required = false) item: String?,
        @RequestParam("vendedor", required = false) vendedor: String?,
        @RequestParam("agrupador", required = false) agrupador: String?
    ): NextLink<PainelIntegradoVendas>? {
        return painelIntegradoVendasServices.fullSearchPedidos(dataInicial,dataFinal,filial,cliente,item,vendedor,agrupador)
    }
}