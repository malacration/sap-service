package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.model.sovis.Produto
import br.andrew.sap.services.OrdersService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService) {


    @PostMapping("")
    fun save(@RequestBody pedido : PedidoVenda): OrderSales {
        val order = ordersService.save(pedido.getOrder()).tryGetValue<OrderSales>()
        try{
            ordersService.aplicaDesonerado(order)
        }catch (e : Throwable){
            e.printStackTrace()
        }
        return order
    }

    @GetMapping("")
    fun get(): List<OrderSales> {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC))).tryGetValues<OrderSales>()
    }

    @GetMapping("data")
    fun getData(): OData {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC)))
    }
}
