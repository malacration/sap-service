package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.impostos.ImpostosDesonerados
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.documents.CreditNotesService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.documents.QuotationsService
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
    val quotationsService : QuotationsService,
    val impostosDesonerados : ImpostosDesonerados) {

    @GetMapping("/{id}/saida")
    fun entrada(@PathVariable id: Int, page : Pageable): Page<Invoice>?{
        return invoiceService.findInvoiceById(id, page)
    }

    @GetMapping("/pedidos/{idContrato}")
    fun pedidos(@PathVariable idContrato: Int): List<Document> {
        val filter = Filter(Predicate("U_venda_futura", idContrato, Condicao.EQUAL),
            Predicate("DownPaymentAmountSC", 0, Condicao.EQUAL))
        return listOf(ordersService,quotationsService)
            .map { it.getAll(Document::class.java,filter) }
            .flatMap { it }
            // Mesmo preenchimento de /entregas: sem ele o LineTotalDesonerado volta null e a
            // aba Pedidos do contrato mostra "R$ ∞" no total, alem de nao bater com a aba
            // Entregas, que ja vem liquida.
            .onEach { it.preencheDesonerado(impostosDesonerados.ids) }
            .sortedWith(compareBy(
                { it.docDate },
                { it.docObjectCode?.ordinal }
            ))
    }
}