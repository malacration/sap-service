package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.documents.Invoice
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
class FutureDeliverySalesController(val invoiceService: InvoiceService) {

    @GetMapping("/{id}/entrada")
    fun entrada(@PathVariable id: Int, page : Pageable): Page<Invoice>?{
        return invoiceService.findInvoiceById(id, page)
    }

    @GetMapping("contrato-venda-futura/")
    fun get(auth : Authentication, @RequestParam docEntry: Int): ResponseEntity<List<Invoice>> {
        if(!(auth is User))
            return ResponseEntity.noContent().build()
        val predicados = mutableListOf(
            Predicate("U_venda_futura", docEntry, Condicao.EQUAL),
        )
        return ResponseEntity.ok(invoiceService
            .get(Filter(predicados), OrderBy(mapOf("DocEntry" to Order.DESC)))
            .tryGetValues<Invoice>()
        )
    }
}