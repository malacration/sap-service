package br.andrew.sap.controllers.invent

import br.andrew.sap.controllers.handler.ErroDto
import br.andrew.sap.model.fiscal.ArquivoGrandeDemaisException
import br.andrew.sap.model.fiscal.CessionariaEditRequest
import br.andrew.sap.model.fiscal.CessionariaInvalidaException
import br.andrew.sap.model.fiscal.ImportacaoInvalidaException
import br.andrew.sap.model.fiscal.CessionariaLote
import br.andrew.sap.model.fiscal.CessionariaRequest
import br.andrew.sap.model.fiscal.ModoImportacaoLote
import br.andrew.sap.services.fiscal.NotaFiscalLoteCessionariaService
import br.andrew.sap.model.fiscal.NotaFiscalLoteFiltro
import br.andrew.sap.model.fiscal.NotaFiscalLoteRequest
import br.andrew.sap.model.fiscal.NotaFiscalResumo
import br.andrew.sap.model.fiscal.StatusSapNota
import br.andrew.sap.model.fiscal.StatusSefaz
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.partner.BusinessPartnerSlin
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.model.fiscal.FormaPagamentoLote
import br.andrew.sap.model.fiscal.CategoriaDocumentoLote
import br.andrew.sap.model.fiscal.TrocaFormaPagamentoRequest
import br.andrew.sap.model.fiscal.TrocaFormaPagamentoResultado
import br.andrew.sap.services.fiscal.NotaFiscalLoteFormaPagamentoService
import br.andrew.sap.services.fiscal.NotaFiscalLoteService
import br.andrew.sap.services.fiscal.NotaFiscalLoteImportacaoService
import br.andrew.sap.services.fiscal.PlanilhaCsv
import br.andrew.sap.services.fiscal.PlanilhaXlsx
import jakarta.servlet.http.HttpServletRequest
import br.andrew.sap.model.fiscal.ImportacaoLoteResultado
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.nio.file.Files
import java.nio.file.StandardOpenOption

// Fora de /tax de proposito: o perfil business_partner libera /tax/**/pdf para o cliente baixar a
// propria nota, e um /tax/lote/... cairia nessa regra.
@RestController
@RequestMapping("nota-fiscal-lote")
class NotaFiscalLoteController(val service: NotaFiscalLoteService,
                               val businessPartnersService: BusinessPartnersService,
                               val formaPagamentoService: NotaFiscalLoteFormaPagamentoService,
                               val importacaoService: NotaFiscalLoteImportacaoService,
                               val cessionariaService: NotaFiscalLoteCessionariaService) {

    @GetMapping("")
    fun listar(@RequestParam(required = false) dataInicial: String? = null,
               @RequestParam(required = false) dataFinal: String? = null,
               @RequestParam(required = false) filial: List<Int>? = null,
               @RequestParam(required = false) cardCode: String? = null,
               @RequestParam(required = false) salesPersonCode: Int? = null,
               @RequestParam(required = false) numeroDe: Int? = null,
               @RequestParam(required = false) numeroAte: Int? = null,
               @RequestParam(required = false) vencimentoDe: String? = null,
               @RequestParam(required = false) vencimentoAte: String? = null,
               @RequestParam(required = false) statusSap: StatusSapNota? = null,
               @RequestParam(required = false) statusSefaz: Int? = null,
               @RequestParam(required = false) categoria: CategoriaDocumentoLote? = null,
               @RequestParam(required = false) formaPagamento: String? = null,
               @RequestParam(required = false) boletoStatus: Int? = null,
               @RequestParam(required = false) porDocumento: Boolean? = null,
               page: Pageable): Page<NotaFiscalResumo> {
        return service.listar(NotaFiscalLoteFiltro(
            dataInicial = dataInicial, dataFinal = dataFinal, filial = filial, cardCode = cardCode,
            salesPersonCode = salesPersonCode, numeroDe = numeroDe, numeroAte = numeroAte,
            vencimentoDe = vencimentoDe, vencimentoAte = vencimentoAte,
            statusSap = statusSap, statusSefaz = statusSefaz, categoria = categoria, formaPagamento = formaPagamento,
            boletoStatus = boletoStatus,
            porDocumento = porDocumento == true), page)
    }

    @GetMapping("status-sefaz")
    fun statusSefaz(): List<StatusSefaz> = service.statusSefaz()

    @GetMapping("status-boleto")
    fun statusBoleto(): List<StatusSefaz> = service.statusBoleto()

    // Planilha de titulos do banco (.xlsx ou .csv, arquivo cru no corpo): devolve os documentos para
    // o lote e o que aconteceu com cada linha. Nao grava nada. O corpo e lido com teto (nao pelo
    // conversor, que carregaria tudo antes de alguem conferir o tamanho).
    // modo = NOSSO_NUMERO (padrao) ou NUMERO_NOTA: acha a NF por numero + parcela + valor + vencimento
    // entre TODAS as NF elegiveis. Os filtros abaixo sao aceitos so por compatibilidade e ignorados.
    @PostMapping("importar-planilha")
    fun importarPlanilha(request: HttpServletRequest,
                         @RequestParam(required = false) modo: ModoImportacaoLote? = null,
                         @RequestParam(required = false) dataInicial: String? = null,
                         @RequestParam(required = false) dataFinal: String? = null,
                         @RequestParam(required = false) filial: List<Int>? = null,
                         @RequestParam(required = false) cardCode: String? = null,
                         @RequestParam(required = false) salesPersonCode: Int? = null,
                         @RequestParam(required = false) numeroDe: Int? = null,
                         @RequestParam(required = false) numeroAte: Int? = null,
                         @RequestParam(required = false) vencimentoDe: String? = null,
                         @RequestParam(required = false) vencimentoAte: String? = null,
                         @RequestParam(required = false) statusSap: StatusSapNota? = null,
                         @RequestParam(required = false) statusSefaz: Int? = null,
                         @RequestParam(required = false) categoria: CategoriaDocumentoLote? = null,
                         @RequestParam(required = false) formaPagamento: String? = null,
                         @RequestParam(required = false) boletoStatus: Int? = null): ImportacaoLoteResultado {
        val teto = PlanilhaXlsx.TETO_ARQUIVO
        // Aqui ainda nao se sabe se e CSV ou XLSX: a mensagem traz os dois limites.
        val grande = ArquivoGrandeDemaisException("A planilha e grande demais (limite de ${PlanilhaCsv.TETO_CSV / 1024 / 1024} MB para CSV e ${teto / 1024 / 1024} MB para XLSX).")
        if (request.contentLengthLong > teto)
            throw grande
        val conteudo = request.inputStream.readNBytes(teto + 1)
        if (conteudo.size > teto)
            throw grande
        if (modo != ModoImportacaoLote.NUMERO_NOTA)
            return importacaoService.importar(conteudo)
        return importacaoService.importarPorNumeroNota(conteudo)
    }

    // todas = tambem as inativas (filtro da lista); sem ele, so as que a troca aceita.
    @GetMapping("formas-pagamento")
    fun formasPagamento(@RequestParam(defaultValue = "false") todas: Boolean): List<FormaPagamentoLote> =
        formaPagamentoService.formas(todas)

    // Cessionarias ativas para o comunicado de cessao (so codigo e nome; ver CessionariaLote).
    @GetMapping("cessionarias")
    fun cessionarias(): List<CessionariaLote> = service.cessionarias()

    // Cadastra uma cessionaria no UDO NFL_CESSIONARIA (nasce ativa, aparece no GET acima).
    @PostMapping("cessionarias")
    fun criarCessionaria(@RequestBody request: CessionariaRequest): CessionariaLote =
        cessionariaService.criar(request)

    // Edita nome e nome do PDF; 404 se o codigo (CNPJ) nao existe.
    @PatchMapping("cessionarias/{codigo}")
    fun editarCessionaria(@PathVariable codigo: String, @RequestBody request: CessionariaEditRequest): CessionariaLote =
        cessionariaService.editar(codigo, request)

    // 400 (entrada invalida), 404 (nao existe) ou 409 (CNPJ repetido) no corpo padrao de erro da API.
    @ExceptionHandler(CessionariaInvalidaException::class)
    fun onCessionariaInvalida(e: CessionariaInvalidaException): ResponseEntity<ErroDto> =
        ResponseEntity.status(e.status).body(ErroDto(e.message ?: "Cessionaria invalida", ""))

    // Erro de entrada da importacao (arquivo/cabecalho/CSV): 400; arquivo acima do teto: 413. So estas excecoes;
    // falha do ODBC/SAP segue pelo tratamento global.
    @ExceptionHandler(ImportacaoInvalidaException::class)
    fun onImportacaoInvalida(e: ImportacaoInvalidaException): ResponseEntity<ErroDto> =
        ResponseEntity.badRequest().body(ErroDto(e.message ?: "Planilha invalida", ""))

    @ExceptionHandler(ArquivoGrandeDemaisException::class)
    fun onArquivoGrande(e: ArquivoGrandeDemaisException): ResponseEntity<ErroDto> =
        ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ErroDto(e.message ?: "Arquivo grande demais", ""))

    // Parametro com valor que nao converte (ex.: modo desconhecido): 400 em vez de 500.
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun onParametroInvalido(e: MethodArgumentTypeMismatchException): ResponseEntity<ErroDto> =
        ResponseEntity.badRequest().body(ErroDto("Parametro '${e.name}' invalido.", ""))

    // Corpo vazio ou JSON malformado: 400 em vez de 500 (vale so para este controller).
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun onCorpoIlegivel(e: HttpMessageNotReadableException): ResponseEntity<ErroDto> =
        ResponseEntity.badRequest().body(ErroDto("Corpo da requisicao invalido.", ""))

    // Grava no SAP (documento e, se preciso, cadastro do parceiro). Um resultado por documento.
    @PostMapping("forma-pagamento")
    fun trocarFormaPagamento(@RequestBody request: TrocaFormaPagamentoRequest,
                             auth: Authentication?): List<TrocaFormaPagamentoResultado> {
        val usuario = (auth as? User)?.let { "${it.name} (${it.id})" } ?: auth?.name ?: "desconhecido"
        return formaPagamentoService.trocar(request, usuario)
    }

    // Filtro de cliente da tela. O /business-partners/search recorta pela carteira do vendedor
    // (so admin e vendedor_admin veem todos), e quem tem so este perfil nao acharia o cliente
    // cujas notas a tela ja lista. Mesmo corpo e resposta do /business-partners/search.
    @PostMapping("clientes/search")
    fun buscarCliente(@RequestBody keyWord: String, auth: Authentication): NextLink<BusinessPartnerSlin> {
        if (auth !is User)
            return NextLink(listOf(), "")
        return businessPartnersService.fullSearchTextTodosClientes(keyWord, auth)
    }

    // Sem produces fixo: com produces = zip o erro em JSON do ControllerAdvice nao teria conversor.
    @PostMapping("download")
    fun download(@RequestBody request: NotaFiscalLoteRequest): ResponseEntity<Resource> {
        val arquivo = service.zip(request.docEntries, request.adiantamentos, request.pdf, request.xml, request.boleto, request.contrato, request.logo, request.termo, request.cessionaria, request.parcelasTermo)
        // DELETE_ON_CLOSE: o conversor do Spring fecha o stream ao terminar de enviar (ou quando
        // o cliente desconecta), e o temporario some junto. Se falhar antes de abrir, apaga aqui.
        val (tamanho, conteudo) = try {
            Files.size(arquivo.caminho) to
                InputStreamResource(Files.newInputStream(arquivo.caminho, StandardOpenOption.DELETE_ON_CLOSE))
        } catch (t: Throwable) {
            Files.deleteIfExists(arquivo.caminho)
            throw t
        }
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/zip"))
            .contentLength(tamanho)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(arquivo.nome).build().toString())
            .header("notas-com-erro", arquivo.notasComErro.toString())
            .header("notas-sem-boleto", arquivo.notasSemBoleto.toString())
            .body(conteudo)
    }
}
