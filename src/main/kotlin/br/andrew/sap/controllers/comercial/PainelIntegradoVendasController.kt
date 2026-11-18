package br.andrew.sap.controllers.comercial

import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.comercial.PainelIntegradoVendas
import br.andrew.sap.services.comercial.PainelIntegradoVendasService
import br.andrew.sap.services.logistica.CarregamentoService
import br.andrew.sap.services.odbc.OdbcException
import org.springframework.http.ResponseEntity
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
        @RequestParam("filial") filial: Int,
        @RequestParam("cliente", required = false) cliente: String?,
        @RequestParam("item", required = false) item: String?,
        @RequestParam("vendedor", required = false) vendedor: Int?,
        @RequestParam("agrupador", required = false) agrupador: String?,
        @RequestParam("localidade", required = false) localidade: String?,
        @RequestParam("incoterms", required = false) incoterms: String?,
        //"pedido" ou "nf-venda-futura"; sem valor traz os dois.
        @RequestParam("tipo", required = false) tipo: String?,
        //"com", "sem" ou "todos"; sem valor traz todos (o painel nunca filtrou frete).
        @RequestParam("frete", required = false) frete: String?
    ): ResponseEntity<Any> {
        // O handler global transforma qualquer excecao em 500: frete invalido responde 400 aqui.
        val sinaisFrete = CarregamentoService.sinaisFrete(frete, "todos")
            ?: return ResponseEntity.badRequest().body(mapOf("mensagem" to "Filtro de frete inválido: use com, sem ou todos."))
        return ResponseEntity.ok(painelIntegradoVendasServices.searchExpeditionOrders(dataInicial,dataFinal,filial,cliente,item,vendedor,agrupador,localidade,incoterms,tipo,sinaisFrete))
    }

    /** Pedidos vem do sap-odbc: resultado_truncado (acima de 10000 linhas) vira 422, como no painel v2. */
    @ExceptionHandler(OdbcException::class)
    fun onOdbc(ex: OdbcException) = respostaOdbc(ex)
}