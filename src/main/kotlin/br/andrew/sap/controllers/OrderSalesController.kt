package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import br.andrew.sap.services.OrdersService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService) {


    @GetMapping
    fun create(): OData {
        val produtos = listOf<Product>(Product("INS0000015","10.5","5.0"))
        val order = OrderSales(
                "CLI0000001", Date(),
                produtos,"2","67")
        return ordersService.save(order)
    }

    @GetMapping("pega")
    fun pega(): OData {
        return ordersService.get()
    }
}