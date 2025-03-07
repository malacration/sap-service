package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.services.stock.ItemsService
import org.springframework.security.core.Authentication

class PurchaseInvoiceforSoftExpert {

    fun prepareToSave(purchaseInvoice: PurchaseInvoice, itemService: ItemsService, auth: Authentication): PurchaseInvoice {
        purchaseInvoice.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
        purchaseInvoice.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
        purchaseInvoice.atualizaPrecoBase(itemService)
        purchaseInvoice.u_pedido_update = "1"
        purchaseInvoice.salesPersonCode = auth.principal.toString().toInt()
        return purchaseInvoice
    }
}