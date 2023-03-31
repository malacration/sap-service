package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.services.OrdersService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService) {


    @PostMapping("")
    fun save(@RequestBody pedido : PedidoVenda): OrderSales {
        val order = ordersService.save(pedido.getOrder()).tryGetValue<OrderSales>()
         ordersService.aplicaDesonerado(order)
        return order
    }

    @GetMapping("pega")
    fun pega(): OData {
        return ordersService.get()
    }
}
