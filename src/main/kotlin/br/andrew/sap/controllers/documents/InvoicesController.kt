package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.BoletoIdsConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.sap.documents.Fatura
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.CarregamentoService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.document.InvoiceOrdemCarregamento
import br.andrew.sap.services.invent.BankPlusService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("invoice")
class InvoicesController(
    val invoice: InvoiceService,
    val bankPlusService : BankPlusService,
    val service : BatchService,
    val carregamentoService : CarregamentoService
) {

    val logger = LoggerFactory.getLogger(InvoicesController::class.java)

    @GetMapping("")
    fun get(pages : Pageable): Any {
        return invoice.get(Filter(),OrderBy(mapOf("DocEntry" to Order.DESC)),pages).tryGetPageValues<Invoice>(pages)
    }

    @GetMapping("raw")
    fun rw(pages : Pageable): Any {
        return invoice.get(Filter(),OrderBy(mapOf("DocEntry" to Order.DESC)),pages)
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String) : Any{
        return invoice.getById("$id")
    }

    //TODO adicionar validacao de cardcode, ao fazer login adicionar o id do parceiro no token
    @GetMapping("/cardcode/{cardcode}/payment")
    fun getByCardCode(@PathVariable cardcode : String,
                      @RequestParam(required = false) numeroNf : Int? = null,
                      @RequestParam(required = false) dataInicial : String? = null,
                      @RequestParam(required = false) dataFinal : String? = null,
                      page : Pageable) : Page<Fatura> {
        val filter = Filter(
            Predicate("CardCode","$cardcode",Condicao.EQUAL),
            Predicate("SequenceModel","39",Condicao.IN),
            Predicate(Cancelled.tNO,Condicao.EQUAL),
        ).also {
            if(numeroNf !=null )
                it.add(Predicate("SequenceSerial",numeroNf,Condicao.EQUAL))
            if(dataInicial !=null )
                it.add(Predicate("DocDate",dataInicial,Condicao.GREAT_EQUAL))
            if(dataFinal !=null )
                it.add(Predicate("DocDate",dataFinal,Condicao.LESS_EQUAL))
        }
        return invoice.get(filter, OrderBy("DocDate",Order.DESC),page)
            .tryGetPageValues<Document>(page).map { Fatura(it,BoletoIdsConfig.ids) }
    }

    @GetMapping("{id}/create-pix")
    fun createPix(@PathVariable id : Int) : Any{
        return invoice.createPix(id)
    }

    @GetMapping("{id}/baixa-pix")
    fun baixaPix(@PathVariable id : Int) : Any{
        return invoice.baixaPixBy(DocEntry(id))
    }

    @GetMapping("{id}/boletos")
    fun getBoleto(@PathVariable id : String) : List<Boleto>{
        val invoice = invoice.getById("$id").tryGetValue<Invoice>()
        return bankPlusService.getBoletosBy(
            invoice.getBPL_IDAssignedToInvoice(),
            invoice.docEntry.toString())
    }

    @GetMapping("/{id}/parcela/{idParcela}", produces = ["application/pdf"])
    fun getPdf(@PathVariable id : String, @PathVariable idParcela : String) : ByteArray? {
        val boletos = getBoleto(id)
        val boleto = boletos.firstOrNull{ it.numeroDaParcela.toString() == idParcela } ?: throw Exception("Boleto nao encontrado")
        return bankPlusService.getPdf(boleto.id.toString())
    }

    @GetMapping("pix")
    fun teste() : Any{
        return invoice.getAllPixs();
    }

    @PostMapping("criar")
    fun criarNotaFiscalEntrada(@RequestBody notaFiscal: Invoice): ResponseEntity<Any> {
        try {
            val hasUsage16 = notaFiscal.DocumentLines.any { it.Usage == 16 }
            notaFiscal.ReserveInvoice = if (hasUsage16) "tYES" else "tNO"

            val document = InvoiceOrdemCarregamento().prepareToSave(notaFiscal)

            val notaCriada = invoice.save(document).tryGetValue<Invoice>()

            val ordemUpdate = mapOf(
                "DocEntry" to notaFiscal.U_ordemCarregamento?.toInt(),
                "U_Status" to "Fechado"
            )

            if (notaFiscal.U_ordemCarregamento != null) {
                carregamentoService.update(ordemUpdate, notaFiscal.U_ordemCarregamento.toString())
            }

            return ResponseEntity.ok(notaCriada)
        } catch (e: Exception) {
            logger.error("Erro ao criar nota fiscal", e)
            return ResponseEntity.badRequest().body(mapOf(
                "error" to "Erro ao criar nota fiscal",
                "message" to e.message
            ))
        }
    }
}
