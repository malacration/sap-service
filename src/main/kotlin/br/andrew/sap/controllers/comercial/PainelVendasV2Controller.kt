package br.andrew.sap.controllers.comercial

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.comercial.painel.*
import br.andrew.sap.services.comercial.PainelAcessoException
import br.andrew.sap.services.comercial.PainelVendasV2Service
import br.andrew.sap.services.odbc.OdbcException
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * Painel de vendas v2 - agregacao no banco via sap-odbc.
 *
 * Os parametros `filiais` e `slpCode` sao PEDIDOS; o escopo real e recalculado no
 * service a partir do token. Pedir uma filial nao autorizada nao da erro: ela e
 * simplesmente descartada do recorte.
 */
@RestController
@RequestMapping("painel-vendas/v2")
class PainelVendasV2Controller(private val service: PainelVendasV2Service) {

    @GetMapping("kpis")
    fun kpis(
        auth: Authentication,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate,
        @RequestParam(required = false) filiais: List<Int>?,
        @RequestParam(required = false) slpCode: Int?,
    ): PainelKpis = service.kpis(
        auth as User,
        PainelFiltro(dataInicio, dataFim, filiais ?: listOf(), slpCode),
    )

    @GetMapping("evolucao")
    fun evolucao(
        auth: Authentication,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataInicio: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dataFim: LocalDate,
        @RequestParam(required = false) filiais: List<Int>?,
        @RequestParam(required = false) slpCode: Int?,
        @RequestParam(required = false) granularidade: Granularidade?,
    ): List<PontoEvolucao> = service.evolucao(
        auth as User,
        PainelFiltro(dataInicio, dataFim, filiais ?: listOf(), slpCode, granularidade ?: Granularidade.MES),
    )

    /** Filtro/periodo invalido ou filial nao liberada: erro do cliente, nao do servidor. */
    @ExceptionHandler(PainelAcessoException::class)
    fun onAcesso(ex: PainelAcessoException) = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(mapOf("erro" to "filtro_invalido", "mensagem" to ex.message))

    /**
     * `resultado_truncado` vira 422 para o front poder orientar o usuario a
     * restringir o periodo, em vez de exibir um recorte silencioso dos dados.
     */
    @ExceptionHandler(OdbcException::class)
    fun onOdbc(ex: OdbcException): ResponseEntity<Map<String, String?>> {
        val corpo = mutableMapOf<String, String?>("erro" to ex.codigo, "mensagem" to ex.message)
        // So aparece quando ha causa tecnica segura de exibir. A tela mostra
        // isso como "detalhes tecnicos", recolhido por padrao.
        ex.detalhe?.let { corpo["detalhe"] = it }
        return ResponseEntity
            .status(if (ex.codigo == "resultado_truncado") HttpStatus.UNPROCESSABLE_ENTITY else HttpStatus.BAD_GATEWAY)
            .body(corpo)
    }
}
