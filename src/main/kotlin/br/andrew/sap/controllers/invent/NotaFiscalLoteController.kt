package br.andrew.sap.controllers.invent

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
import br.andrew.sap.services.fiscal.PlanilhaXlsx
import jakarta.servlet.http.HttpServletRequest
import br.andrew.sap.model.fiscal.ImportacaoLoteResultado
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.nio.file.StandardOpenOption

// Fora de /tax de proposito: o perfil business_partner libera /tax/**/pdf para o cliente baixar a
// propria nota, e um /tax/lote/... cairia nessa regra.
@RestController
@RequestMapping("nota-fiscal-lote")
class NotaFiscalLoteController(val service: NotaFiscalLoteService,
                               val businessPartnersService: BusinessPartnersService,
                               val formaPagamentoService: NotaFiscalLoteFormaPagamentoService,
                               val importacaoService: NotaFiscalLoteImportacaoService) {

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

    // Planilha de titulos do banco (.xlsx, arquivo cru no corpo): devolve os documentos para o lote
    // e o que aconteceu com cada linha. Nao grava nada. O corpo e lido com teto (nao pelo conversor,
    // que carregaria tudo antes de alguem conferir o tamanho).
    @PostMapping("importar-planilha")
    fun importarPlanilha(request: HttpServletRequest): ImportacaoLoteResultado {
        val teto = PlanilhaXlsx.TETO_ARQUIVO
        val grande = IllegalArgumentException("A planilha e grande demais (limite de ${teto / 1024 / 1024} MB).")
        if (request.contentLengthLong > teto)
            throw grande
        val conteudo = request.inputStream.readNBytes(teto + 1)
        if (conteudo.size > teto)
            throw grande
        return importacaoService.importar(conteudo)
    }

    // todas = tambem as inativas (filtro da lista); sem ele, so as que a troca aceita.
    @GetMapping("formas-pagamento")
    fun formasPagamento(@RequestParam(defaultValue = "false") todas: Boolean): List<FormaPagamentoLote> =
        formaPagamentoService.formas(todas)

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
        val arquivo = service.zip(request.docEntries, request.adiantamentos, request.pdf, request.xml, request.boleto, request.contrato, request.logo)
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
