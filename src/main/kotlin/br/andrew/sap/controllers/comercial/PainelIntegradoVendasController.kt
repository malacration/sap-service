package br.andrew.sap.controllers.comercial

import br.andrew.sap.services.comercial.PainelIntegradoVendasService
import br.andrew.sap.services.logistica.CarregamentoService
import br.andrew.sap.services.odbc.OdbcException
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
        @RequestParam("filial") filial: List<Int>,
        @RequestParam("cliente", required = false) cliente: String?,
        @RequestParam("item", required = false) item: String?,
        @RequestParam("vendedor", required = false) vendedor: Int?,
        @RequestParam("agrupador", required = false) agrupador: String?,
        @RequestParam("localidade", required = false) localidade: String?,
        @RequestParam("incoterms", required = false) incoterms: String?,
        //"pedido" ou "nf-venda-futura"; sem valor traz os dois.
        @RequestParam("tipo", required = false) tipo: String?,
        //"com", "sem" ou "todos"; sem valor traz todos (o painel nunca filtrou frete).
        @RequestParam("frete", required = false) frete: String?,
        //"sim", "nao" ou "todos": se a linha ja esta numa ordem de carregamento.
        @RequestParam("emOrdem", required = false) emOrdem: String?,
        //Sempre paginado; o tamanho passa de 200 e e limitado la, sem recusar.
        @PageableDefault(size = 50) page: Pageable,
    ): ResponseEntity<Any> {
        validar(filial, tipo, frete, emOrdem)?.let { return it }
        val sinaisFrete = CarregamentoService.sinaisFrete(frete, "todos")!!
        return ResponseEntity.ok(painelIntegradoVendasServices.pedidos(dataInicial, dataFinal, filial, cliente, item, vendedor,
            agrupador, localidade, incoterms, tipo, sinaisFrete, emOrdem, page))
    }

    @GetMapping("pedidos/totais")
    fun totaisPedidos(
        @RequestParam("dataInicial") dataInicial: String,
        @RequestParam("dataFinal")   dataFinal:   String,
        @RequestParam("filial") filial: List<Int>,
        @RequestParam("cliente", required = false) cliente: String?,
        @RequestParam("item", required = false) item: String?,
        @RequestParam("vendedor", required = false) vendedor: Int?,
        @RequestParam("localidade", required = false) localidade: String?,
        @RequestParam("incoterms", required = false) incoterms: String?,
        @RequestParam("tipo", required = false) tipo: String?,
        @RequestParam("frete", required = false) frete: String?,
        @RequestParam("emOrdem", required = false) emOrdem: String?,
    ): ResponseEntity<Any> {
        validar(filial, tipo, frete, emOrdem)?.let { return it }
        val sinaisFrete = CarregamentoService.sinaisFrete(frete, "todos")!!
        return ResponseEntity.ok(painelIntegradoVendasServices.totais(dataInicial, dataFinal, filial, cliente, item, vendedor,
            localidade, incoterms, tipo, sinaisFrete, emOrdem))
    }

    // O handler global transforma qualquer excecao em 500: filtro invalido responde 400 aqui.
    private fun validar(filial: List<Int>, tipo: String?, frete: String?, emOrdem: String?): ResponseEntity<Any>? {
        if (filial.isEmpty()) return recusa("Informe ao menos uma filial.")
        if (CarregamentoService.sinaisFrete(frete, "todos") == null)
            return recusa("Filtro de frete inválido: use com, sem ou todos.")
        if (!tipo.isNullOrEmpty() && tipo !in listOf("pedido", "nf-venda-futura"))
            return recusa("Filtro de tipo inválido: use pedido ou nf-venda-futura.")
        if (!emOrdem.isNullOrEmpty() && emOrdem !in listOf("todos", "sim", "nao"))
            return recusa("Filtro de emOrdem inválido: use todos, sim ou nao.")
        return null
    }

    private fun recusa(mensagem: String): ResponseEntity<Any> = ResponseEntity.badRequest().body(mapOf("mensagem" to mensagem))

    /** Pedidos vem do sap-odbc: resultado_truncado (rede de seguranca, acima de 10000 linhas) vira 422, como no painel v2. */
    @ExceptionHandler(OdbcException::class)
    fun onOdbc(ex: OdbcException) = respostaOdbc(ex)
}