package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.DocEntry
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.documents.DocumentStatus
import br.andrew.sap.model.documents.Fatura
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.services.*
import br.andrew.sap.services.document.InvoiceService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("invoice")
class InvoicesController(
    val invoice: InvoiceService,
    val bankPlusService : BankPlusService) {

    val logger = LoggerFactory.getLogger(InvoicesController::class.java)

    @GetMapping("")
    fun get(): Any {
        return invoice.get(OrderBy(mapOf("DocEntry" to Order.DESC))).tryGetValues<Invoice>()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String) : Any{
        return invoice.getById("$id")
    }

    @GetMapping("/cardcode/{cardcode}/payment")
    fun getByCardCode(@PathVariable cardcode : String, page : Pageable) : Page<Fatura> {
        val filter = Filter(
            Predicate("CardCode","$cardcode",Condicao.EQUAL),
            Predicate("DocumentStatus", DocumentStatus.bost_Open,Condicao.EQUAL)
        )
        return invoice.get(filter, page)
            .tryGetPageValues<Document>().map { Fatura(it) }
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
    fun getPdf(@PathVariable id : String, @PathVariable idParcela : String) : Any? {
        val boletos = getBoleto(id)
        val boleto = boletos.firstOrNull{ it.numeroDaParcela.toString() == idParcela } ?: throw Exception("Boleto nao encontrado")
        return bankPlusService.getPdf(boleto.id.toString())
    }

    @GetMapping("pix")
    fun teste() : Any{
        return invoice.getAllPixs();
    }
}
