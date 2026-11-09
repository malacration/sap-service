package br.andrew.sap.controllers.financeiro

import br.andrew.sap.model.financeiro.retorno.LancamentoRetornoAutomatico
import br.andrew.sap.model.financeiro.retorno.LancamentoRetornoLog
import br.andrew.sap.model.financeiro.retorno.RetornoDescontado
import br.andrew.sap.services.financeiro.retorno.LancamentoRetornoAutomaticoService
import br.andrew.sap.services.financeiro.retorno.LancamentoRetornoLogService
import br.andrew.sap.services.financeiro.retorno.LancamentoRetornoService
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * Lancamento de Retorno: LCM do desconto de duplicatas a partir dos retornos importados no BankPlus.
 *
 * Dois passos: a previa so le e mostra o LCM, o lancar grava. O lancar sempre relê o retorno no
 * BankPlus em vez de confiar na previa que a tela mostrou. O automatico liga e desliga aqui; quem
 * lanca sozinho e a LancamentoRetornoSchedule.
 */
@RestController
@RequestMapping("lancamento-retorno")
class LancamentoRetornoController(
    val service: LancamentoRetornoService,
    val automatico: LancamentoRetornoAutomaticoService,
    val historico: LancamentoRetornoLogService,
) {
    private val log = LoggerFactory.getLogger(LancamentoRetornoController::class.java)

    @GetMapping("retornos")
    fun retornos(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) de: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) ate: LocalDate,
    ): List<RetornoDescontado> = service.listar(de, ate)

    @GetMapping("retornos/{retorno}/previa")
    fun previa(@PathVariable retorno: Int): ResponseEntity<Any> =
        service.previa(retorno)?.let { ResponseEntity.ok(it) } ?: naoEncontrado(retorno)

    @PostMapping("retornos/{retorno}/lancar")
    fun lancar(@PathVariable retorno: Int, auth: Authentication): ResponseEntity<Any> =
        service.lancar(retorno, "MANUAL", auth.name)?.let { ResponseEntity.ok(it) } ?: naoEncontrado(retorno)

    //sem as tabelas LANC_RET_* no SAP (fields.lancamento-retorno nunca ligado no ambiente) o SL
    //responde "Service Not Found". 503 sem corpo: a tela mostra "indisponivel" no proprio card, sem o
    //alerta do ErrorInterceptor (que so aparece quando o erro tem corpo) a cada vez que abre
    @GetMapping("automatico")
    fun automatico(): ResponseEntity<LancamentoRetornoAutomatico> = try {
        ResponseEntity.ok(automatico.estado())
    } catch (e: Exception) {
        log.warn("Automatico do Lancamento de Retorno indisponivel: ${e.message}")
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }

    @PutMapping("automatico")
    fun definirAutomatico(@RequestBody pedido: Map<String, Boolean>, auth: Authentication): LancamentoRetornoAutomatico =
        automatico.definir(pedido["ativo"] == true, auth.name)

    @GetMapping("historico")
    fun historico(): List<LancamentoRetornoLog> = historico.ultimos()

    //mesmo formato {mensagem} do handler global, sem passar por ele (500 e alerta no Telegram)
    private fun naoEncontrado(retorno: Int): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("mensagem" to "Retorno $retorno sem ocorrencia de desconto no BankPlus."))
}
