package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.PainelIntegradoVendas
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.PainelIntegradoVendasService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("painel")
class PainelIntegradoVendasController(
    private val painelIntegradoVendasServices: PainelIntegradoVendasService
) {
    @GetMapping("pedidos")
    fun searchPedidos(
        @RequestParam("dataInicial") dataInicial: String,
        @RequestParam("dataFinal")   dataFinal:   String
    ): NextLink<PainelIntegradoVendas>? {
        return painelIntegradoVendasServices.fullSearchPedidos(dataInicial, dataFinal)
    }
}