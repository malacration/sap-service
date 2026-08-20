package br.andrew.sap.controllers.cobranca

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaAcaoLoteItem
import br.andrew.sap.model.cobranca.CobrancaAcaoRequest
import br.andrew.sap.model.cobranca.CobrancaAcaoResultado
import br.andrew.sap.model.cobranca.CobrancaDashboard
import br.andrew.sap.model.cobranca.CobrancaDominio
import br.andrew.sap.model.cobranca.CobrancaHistoricoLinha
import br.andrew.sap.model.cobranca.CobrancaMes
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaTitulo
import br.andrew.sap.services.cobranca.CobrancaConsultaService
import br.andrew.sap.services.cobranca.CobrancaDashboardService
import br.andrew.sap.services.cobranca.CobrancaDominioService
import br.andrew.sap.services.cobranca.CobrancaService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.YearMonth

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
        // Multi-selecao na tela: filial=6&filial=7. Um valor unico continua chegando aqui
        // como lista de um elemento, entao o link antigo do drill-down segue valendo.
        @RequestParam(required = false) filial: List<Int>?,
        @RequestParam(required = false) vendedor: Int?,
        @RequestParam(required = false) cliente: String?,
        @RequestParam(required = false) data: String?,
        @RequestParam(required = false) status: String?,
        // "1 - NAO INICIADO" e rotulo que a tela usa pra U_Status vazio (o titulo que ninguem
        // trabalhou ainda nao tem registro na UDT). Sem isso, filtrar por ele nao devolve nada.
        @RequestParam(required = false) incluirSemStatus: Boolean?,
        @RequestParam(required = false) cobrador: String?,
        @RequestParam(required = false) situacao: String?,
        @RequestParam(required = false) situacaoSap: String?,
        @RequestParam(required = false) vencimentoDe: String?,
        @RequestParam(required = false) vencimentoAte: String?,
        // Meses de lancamento no formato YYYY-MM. Multi-selecao na tela chega repetido
        // (lancamentoMes=2026-07&lancamentoMes=2026-08); um valor unico continua valendo.
        @RequestParam(required = false) lancamentoMes: List<String>?,
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
            filiais = filial,
            vendedor = vendedor,
            cliente = cliente,
            data = data?.let { LocalDate.parse(it) } ?: LocalDate.now(),
            status = status,
            incluirSemStatus = incluirSemStatus,
            cobrador = cobrador,
            situacao = situacao,
            situacaoSap = situacaoSap,
            vencimentoDe = vencimentoDe?.let { LocalDate.parse(it) },
            vencimentoAte = vencimentoAte?.let { LocalDate.parse(it) },
            lancamentoMeses = lancamentoMes?.map { YearMonth.parse(it) },
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
        auth: Authentication,
    ): ResponseEntity<List<CobrancaHistoricoLinha>> {
        if (auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(service.historico(auth, tipo, docEntry, instlmntId))
    }

    @DeleteMapping("titulos/{tipo}/{docEntry}/{instlmntId}/historico/{lineId}")
    fun removerHistorico(
        @PathVariable tipo: String,
        @PathVariable docEntry: Int,
        @PathVariable instlmntId: Int,
        @PathVariable lineId: Int,
        auth: Authentication,
    ): ResponseEntity<List<CobrancaHistoricoLinha>> {
        // 403 e nao o 204 dos GET vizinhos: em DELETE, 204 e a resposta canonica de sucesso - a
        // tela apagaria a linha da lista acreditando que o SAP tinha apagado tambem.
        if (auth !is User)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        return ResponseEntity.ok(service.removerHistorico(auth, tipo, docEntry, instlmntId, lineId))
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

    @GetMapping("cobradores")
    fun cobradores(auth: Authentication): ResponseEntity<List<String>> {
        if (auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(consultaService.cobradores())
    }

    @PostMapping("dominios")
    fun salvarDominio(@RequestBody dominio: CobrancaDominio): CobrancaDominio {
        return dominioService.salvar(dominio)
    }
}
