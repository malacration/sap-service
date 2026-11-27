package br.andrew.sap.controllers.financeiro

import br.andrew.sap.services.financeiro.contaspagar.AnexoIndisponivelException
import br.andrew.sap.services.financeiro.contaspagar.ConsultaInvalidaException
import br.andrew.sap.services.financeiro.contaspagar.ConsultaPagamentoParcelaRequest
import br.andrew.sap.services.financeiro.contaspagar.ConsultaPagamentoParcelaResponse
import br.andrew.sap.services.financeiro.contaspagar.ConsultaPagamentoParcelaService
import br.andrew.sap.services.financeiro.contaspagar.FilialSap
import br.andrew.sap.services.financeiro.contaspagar.ListagemNotasEntradaRequest
import br.andrew.sap.services.financeiro.contaspagar.ListagemNotasEntradaResponse
import br.andrew.sap.services.financeiro.contaspagar.NotaEntradaNaoEncontradaException
import br.andrew.sap.services.financeiro.contaspagar.NotasEntradaService
import br.andrew.sap.services.odbc.OdbcException
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Conferencia de pagamento de parcela de nota de entrada, consumida pelo Nota Agil (fluxo FXP-NA
 * do SoftExpert). Somente leitura no SAP: Service Layer (filial) e sap-odbc (fornecedor, nota,
 * parcelas e pagamentos).
 *
 * POST so para levar CNPJ/numero no corpo em vez da URL (fora dos logs de acesso); nao altera nada.
 * Autorizacao pelo rules.yml: perfil `integracao_contas_pagar`.
 */
@RestController
@RequestMapping("contas-pagar")
class ContasPagarController(
    private val service: ConsultaPagamentoParcelaService,
    private val notasEntrada: NotasEntradaService,
) {

    @PostMapping("pagamento-parcela/consulta")
    fun consultarPagamentoParcela(@RequestBody request: ConsultaPagamentoParcelaRequest): ConsultaPagamentoParcelaResponse =
        service.consultar(request)

    /**
     * Notas fiscais de entrada de uma filial (periodo de lancamento ou DocEntry escolhidos), para o
     * Nota Agil importar como fonte alternativa ao Qive. POST para o CNPJ ir no corpo.
     */
    /**
     * Filiais do SAP (BusinessPlaces) com CNPJ, para o Nota Agil cadastrar as empresas. Somente leitura.
     * Filial sem CNPJ valido (14 digitos) fica de fora.
     */
    @GetMapping("filiais")
    fun filiais(): List<FilialSap> = notasEntrada.filiais()

    @PostMapping("notas-entrada/listagem")
    fun listarNotasEntrada(@RequestBody request: ListagemNotasEntradaRequest): ListagemNotasEntradaResponse =
        notasEntrada.listar(request)

    /** PDF anexado a nota de entrada (anexo do SAP). Corpo: `{"cnpjFilial": "..."}`; 404 sem PDF. */
    @PostMapping("notas-entrada/{docEntry}/anexo-pdf")
    fun anexoPdfNotaEntrada(@PathVariable docEntry: Int, @RequestBody corpo: AnexoPdfRequest): ResponseEntity<ByteArray> {
        val (arquivo, bytes) = notasEntrada.anexoPdf(docEntry, corpo.cnpjFilial)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(arquivo).build().toString())
            .body(bytes)
    }

    data class AnexoPdfRequest(val cnpjFilial: String? = null)

    @ExceptionHandler(AnexoIndisponivelException::class)
    fun anexoIndisponivel(e: AnexoIndisponivelException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(mapOf("erro" to "anexo_indisponivel", "mensagem" to (e.message ?: "Anexo indisponivel")))

    @ExceptionHandler(NotaEntradaNaoEncontradaException::class)
    fun naoEncontrada(e: NotaEntradaNaoEncontradaException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("erro" to "nao_encontrado", "mensagem" to (e.message ?: "Nao encontrado")))

    @ExceptionHandler(ConsultaInvalidaException::class)
    fun consultaInvalida(e: ConsultaInvalidaException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("erro" to "consulta_invalida", "mensagem" to (e.message ?: "Consulta invalida")))

    /**
     * Falha do sap-odbc (indisponivel, timeout, resultado truncado): 502, para o Nota Agil tratar
     * como falha tecnica. A mensagem do OdbcClient ja vem sem SQL nem dado consultado.
     */
    @ExceptionHandler(OdbcException::class)
    fun odbc(e: OdbcException): ResponseEntity<Map<String, String>> =
        // Listagem grande demais (periodo amplo): erro de filtro do usuario, nao falha do SAP.
        if (e.codigo == "resultado_truncado") ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(mapOf("erro" to e.codigo, "mensagem" to "Muitas notas no periodo; restrinja o periodo ou os filtros"))
        else ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(mapOf("erro" to e.codigo, "mensagem" to "Falha na consulta ao SAP (sap-odbc): ${e.message}"))
}
