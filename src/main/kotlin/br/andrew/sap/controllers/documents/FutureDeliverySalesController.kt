package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.document.QuotationsService
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
class FutureDeliverySalesController(
    val invoiceService: InvoiceService,
    val creditNotesService: CreditNotesService,
    val ordersService: OrdersService,
    val quotationsService : QuotationsService) {

    @GetMapping("/{id}/saida")
    fun entrada(@PathVariable id: Int, page : Pageable): Page<Invoice>?{
        return invoiceService.findInvoiceById(id, page)
    }

    @GetMapping("/contrato-venda-futura/{idContrato}")
    fun get(@PathVariable idContrato: Int): List<Document> {
        val filter = Filter(Predicate("U_venda_futura", idContrato, Condicao.EQUAL),
            Predicate("DownPaymentAmountSC", 0, Condicao.EQUAL))
        return listOf(creditNotesService,invoiceService)
            .map { it.getAll(Document::class.java,filter) }
            .flatMap { it }
            .filter{ it.DocumentLines.none { it.FatherType != "cPayments_sum"}}
            .sortedWith(compareBy(
                { it.docDate },
                { it.docObjectCode?.ordinal }
            ))
    }

    @GetMapping("/pedidos/{idContrato}")
    fun pedidos(@PathVariable idContrato: Int): List<Document> {
        val filter = Filter(Predicate("U_venda_futura", idContrato, Condicao.EQUAL),
            Predicate("DownPaymentAmountSC", 0, Condicao.EQUAL))
        return listOf(ordersService,quotationsService)
            .map { it.getAll(Document::class.java,filter) }
            .flatMap { it }
            .sortedWith(compareBy(
                { it.docDate },
                { it.docObjectCode?.ordinal }
            ))
    }
}