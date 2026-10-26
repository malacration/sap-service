package br.andrew.sap.controllers.logistica

import br.andrew.sap.model.logistica.DepositoNecessidade
import br.andrew.sap.model.logistica.DetalheNecessidade
import br.andrew.sap.model.logistica.NecessidadeProducao
import br.andrew.sap.model.logistica.NecessidadeProducaoDetalhe
import br.andrew.sap.services.logistica.NecessidadeProducaoException
import br.andrew.sap.services.logistica.NecessidadeProducaoService
import br.andrew.sap.services.odbc.OdbcException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("relatorios/necessidade-producao")
class NecessidadeProducaoController(private val service: NecessidadeProducaoService) {

    /** Depositos em que a faixa de produtos esta cadastrada, para montar os filtros. */
    @GetMapping("depositos")
    fun depositos(@RequestParam itemDe: String, @RequestParam itemAte: String): List<DepositoNecessidade> =
        service.depositos(itemDe, itemAte)

    @GetMapping
    fun consultar(
        @RequestParam itemDe: String,
        @RequestParam itemAte: String,
        @RequestParam(required = false) filiais: List<Int>?,
        @RequestParam(required = false) depositos: List<String>?,
    ): NecessidadeProducao = service.consultar(itemDe, itemAte, filiais ?: listOf(), depositos ?: listOf())

    /** Documentos de uma celula: detalhe=PEDIDO (coluna Pedido) ou SEM_PREVISAO. So a venda, entao so filiais. */
    @GetMapping("detalhe")
    fun detalhe(
        @RequestParam itemCode: String,
        @RequestParam detalhe: DetalheNecessidade,
        @RequestParam(required = false) filiais: List<Int>?,
    ): NecessidadeProducaoDetalhe = service.detalhe(itemCode, detalhe, filiais ?: listOf())

    @ExceptionHandler(NecessidadeProducaoException::class)
    fun onFiltro(ex: NecessidadeProducaoException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(mapOf("erro" to "filtro_invalido", "mensagem" to ex.message))

    @ExceptionHandler(OdbcException::class)
    fun onOdbc(ex: OdbcException) = ResponseEntity
        .status(if (ex.codigo == "resultado_truncado") HttpStatus.UNPROCESSABLE_ENTITY else HttpStatus.BAD_GATEWAY)
        .body(mapOf("erro" to ex.codigo, "mensagem" to ex.message))
}
