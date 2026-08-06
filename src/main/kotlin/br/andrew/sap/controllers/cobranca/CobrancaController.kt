package br.andrew.sap.controllers.cobranca

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaAcaoLoteItem
import br.andrew.sap.model.cobranca.CobrancaAcaoRequest
import br.andrew.sap.model.cobranca.CobrancaAcaoResultado
import br.andrew.sap.model.cobranca.CobrancaDashboard
import br.andrew.sap.model.cobranca.CobrancaDominio
import br.andrew.sap.model.cobranca.CobrancaHistorico
import br.andrew.sap.model.cobranca.CobrancaMes
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaTitulo
import br.andrew.sap.services.cobranca.CobrancaConsultaService
import br.andrew.sap.services.cobranca.CobrancaDashboardService
import br.andrew.sap.services.cobranca.CobrancaDominioService
import br.andrew.sap.services.cobranca.CobrancaService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("cobranca")
class CobrancaController(
    val consultaService: CobrancaConsultaService,
    val service: CobrancaService,
    val dominioService: CobrancaDominioService,
    val dashboardService: CobrancaDashboardService,
) {

    @GetMapping("titulos")
    fun titulos(
        auth: Authentication,
        @RequestParam(required = false) filial: Int?,
        @RequestParam(required = false) vendedor: Int?,
        @RequestParam(required = false) cliente: String?,
        @RequestParam(required = false) data: String?,
        @RequestParam(required = false) diasAtrasoMin: Int?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) cobrador: String?,
        @RequestParam(required = false) situacao: String?,
        @RequestParam(required = false) situacaoSap: String?,
        @RequestParam(required = false) vencimentoDe: String?,
        @RequestParam(required = false) vencimentoAte: String?,
        @RequestParam(required = false) semAcompanhamento: Boolean?,
        @RequestParam(required = false) promessaVencidaAte: String?,
        @RequestParam(required = false) tipo: String?,
        @RequestParam(defaultValue = "0") pagina: Int,
        @RequestParam(defaultValue = "20") tamanho: Int,
    ): ResponseEntity<List<CobrancaTitulo>> {
        if (auth !is User)
            return ResponseEntity.noContent().build()

        val resultado = consultaService.listar(
            auth = auth,
            filial = filial,
            vendedor = vendedor,
            cliente = cliente,
            data = data?.let { LocalDate.parse(it) } ?: LocalDate.now(),
            diasAtrasoMin = diasAtrasoMin,
            status = status,
            cobrador = cobrador,
            situacao = situacao,
            situacaoSap = situacaoSap,
            vencimentoDe = vencimentoDe?.let { LocalDate.parse(it) },
            vencimentoAte = vencimentoAte?.let { LocalDate.parse(it) },
            semAcompanhamento = semAcompanhamento,
            promessaVencidaAte = promessaVencidaAte?.let { LocalDate.parse(it) },
            tipo = tipo,
            pagina = pagina,
            tamanhoPagina = tamanho,
        )
        return ResponseEntity.ok(resultado)
    }

    @GetMapping("titulos/{tipo}/{docEntry}/{instlmntId}/historico")
    fun historico(
        @PathVariable tipo: String,
        @PathVariable docEntry: Int,
        @PathVariable instlmntId: Int,
    ): List<CobrancaHistorico> {
        return service.historico(tipo, docEntry, instlmntId)
    }

    @PostMapping("titulos/{tipo}/{docEntry}/{instlmntId}/acao")
    fun registrarAcao(
        @PathVariable tipo: String,
        @PathVariable docEntry: Int,
        @PathVariable instlmntId: Int,
        @RequestBody req: CobrancaAcaoRequest,
        auth: Authentication,
    ): ResponseEntity<CobrancaRegistro> {
        if (auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(service.registrarAcao(tipo, docEntry, instlmntId, req, auth))
    }

    @PostMapping("titulos/acoes")
    fun registrarAcaoEmLote(
        @RequestBody itens: List<CobrancaAcaoLoteItem>,
        auth: Authentication,
    ): ResponseEntity<List<CobrancaAcaoResultado>> {
        if (auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(service.registrarAcaoEmLote(itens, auth))
    }

    // O dashboard vem em duas rotas de proposito: o resumo custa ~16 consultas ao SAP e a
    // serie mensal custa mais uma paginada, entao separando a tela pinta em duas ondas em
    // vez de esperar tudo pra mostrar o primeiro numero.
    @GetMapping("dashboard")
    fun dashboard(
        auth: Authentication,
        @RequestParam(required = false) filial: Int?,
        @RequestParam(required = false) vendedor: Int?,
        @RequestParam(required = false) de: String?,
        @RequestParam(required = false) ate: String?,
    ): ResponseEntity<CobrancaDashboard> {
        if (auth !is User)
            return ResponseEntity.noContent().build()
        val hoje = LocalDate.now()
        return ResponseEntity.ok(
            dashboardService.resumo(
                auth = auth,
                filial = filial,
                vendedor = vendedor,
                de = de?.let { LocalDate.parse(it) } ?: hoje.withDayOfMonth(1),
                ate = ate?.let { LocalDate.parse(it) } ?: hoje,
                hoje = hoje,
            )
        )
    }

    @GetMapping("dashboard/evolucao")
    fun evolucao(
        auth: Authentication,
        @RequestParam(required = false) filial: Int?,
        @RequestParam(required = false) vendedor: Int?,
        @RequestParam(defaultValue = "6") meses: Int,
    ): ResponseEntity<List<CobrancaMes>> {
        if (auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(
            dashboardService.evolucao(
                auth = auth,
                filial = filial,
                vendedor = vendedor,
                meses = meses,
                hoje = LocalDate.now(),
            )
        )
    }

    @GetMapping("dominios")
    fun dominios(@RequestParam(required = false) tipo: String?): List<CobrancaDominio> {
        return dominioService.listar(tipo)
    }

    @PostMapping("dominios")
    fun salvarDominio(@RequestBody dominio: CobrancaDominio): CobrancaDominio {
        return dominioService.salvar(dominio)
    }
}
