package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.services.OrdersService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService) {


    @PostMapping("")
    fun save(@RequestBody pedido : PedidoVenda): OData {
        return ordersService.save(pedido.getOrder())
    }

    @GetMapping("pega")
    fun pega(): OData {
        return ordersService.get()
    }
}