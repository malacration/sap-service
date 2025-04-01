package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.InvoiceService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("future-sales")
class FutureDeliverySalesController(val invoiceService: InvoiceService,
    val creditNotesService: CreditNotesService) {

    @GetMapping("/{id}/saida")
    fun entrada(@PathVariable id: Int, page : Pageable): Page<Invoice>?{
        return invoiceService.findInvoiceById(id, page)
    }

    @GetMapping("/contrato-venda-futura/{idContrato}")
    fun get(auth: Authentication, @PathVariable idContrato: Int): ResponseEntity<List<Document>> {
        if (!(auth is User)) {
            return ResponseEntity.noContent().build()
        }

        val predicados = mutableListOf(
            Predicate("U_venda_futura", idContrato, Condicao.EQUAL),
            Predicate("DownPaymentAmountSC", 0, Condicao.EQUAL)
        )
        val filter = Filter(predicados)

        val creditNotes = creditNotesService.getAll(Document::class.java,filter)
        val invoices = invoiceService.getAll(Document::class.java,filter)
        val listaCombinada = creditNotes + invoices

        return ResponseEntity.ok(listaCombinada.sortedBy { it.docDate })
    }
}