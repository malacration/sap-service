package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.document.DownPaymentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("down-payment")
class DownPaymentController(val service : DownPaymentService) {


    @GetMapping("contrato-venda-futura/{id}")
    fun getByContrato(@PathVariable id : Int): List<Document> {
        return service.getByContratoVendaFutura(id,
            OrderBy(mapOf("DocumentStatus" to Order.DESC,"DocDueDate" to Order.ASC,"DocEntry" to Order.ASC))
        )
    }
}