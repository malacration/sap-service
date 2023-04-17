package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.exceptions.LinkedPaymentMethodException
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.services.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService,
                           val priceList : PriceListsService,
                           val businesPartner : BusinessPartnersService,
                           val itemService : ItemsService) {

    @PostMapping("")
    fun save(@RequestBody pedido : PedidoVenda): OrderSales {
        try {
            val order = ordersService.save(
                    pedido.getOrder().also { it.aplicaBase(itemService) }
            ).tryGetValue<OrderSales>()
            try{
                ordersService.aplicaDesonerado(order)
            }catch (e : Throwable){
                e.printStackTrace()
            }
            return order
        }catch (t : LinkedPaymentMethodException){
            businesPartner.addPaymentMethod(pedido.idCliente,pedido.idFormaPagamento)
            throw t
        }
    }

    @GetMapping("")
    fun get(): List<OrderSales> {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC))).tryGetValues<OrderSales>()
    }

    @GetMapping("data")
    fun getData(): OData {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC)))
    }

    @GetMapping("teste")
    fun tete(@Value("{base.price.list.name:none}") list : String): Any {
        return itemService.getPriceBase("PAC0000105")
    }
}
