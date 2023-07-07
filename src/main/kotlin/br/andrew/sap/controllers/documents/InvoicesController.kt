package br.andrew.sap.controllers.documents

import br.andrew.sap.events.DraftOrderSalesSaveEvent
import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.services.*
import br.andrew.sap.services.document.InvoiceService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("invoice")
class InvoicesController(val invoice: InvoiceService) {

    val logger = LoggerFactory.getLogger(InvoicesController::class.java)

    @GetMapping("")
    fun get(): Any {
        return invoice.get(OrderBy(mapOf("DocEntry" to Order.DESC)))
    }
}
