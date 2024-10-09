package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.ItemsService
import org.springframework.security.core.Authentication

class DocumentForAngular {

    fun prepareToSave(pedido : Document, itemService: ItemsService, auth : Authentication): Document {
        pedido.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
        pedido.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
        pedido.atualizaPrecoBase(itemService)
        pedido.u_pedido_update = "1"
        pedido.salesPersonCode = auth.principal.toString().toInt()
        return pedido
    }
}