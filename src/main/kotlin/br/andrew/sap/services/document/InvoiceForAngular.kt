package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.services.stock.ItemsService
import org.springframework.security.core.Authentication

class InvoiceForAngular {

    fun prepareToSave(order: OrderSales): Invoice {
        val invoice = Invoice(
            CardCode = order.CardCode,
            DocDueDate = order.DocDueDate,
            DocumentLines = order.DocumentLines.map { it.Duplicate() },
            BPL_IDAssignedToInvoice = order.getBPL_IDAssignedToInvoice()
        ).apply {
            comments = order.comments
            salesPersonCode = order.salesPersonCode
            paymentGroupCode = order.paymentGroupCode
            u_id_pedido_forca = order.u_id_pedido_forca
            u_uuid_forca = order.u_uuid_forca
            docDate = order.docDate
            journalMemo = order.journalMemo ?: getDefaultForJournal(this, "Nota fiscal de Saída")
            model = order.model
            docType = order.docType
            shipToCode = order.shipToCode
            Address = order.Address
        }

        return invoice
    }
}