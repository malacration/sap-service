package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import br.andrew.sap.model.exceptions.LinkedPaymentMethodException
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.model.sovis.Produto
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinesPlaceService
import br.andrew.sap.services.DummyService
import br.andrew.sap.services.OrdersService
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService,
                           val businesPartner : BusinessPartnersService,
                           val dumy : DummyService) {


    @PostMapping("")
    fun save(@RequestBody pedido : PedidoVenda): OrderSales {
        try {
            val order = ordersService.save(pedido.getOrder()).tryGetValue<OrderSales>()
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

    @GetMapping("try")
    fun getUpdate(): OrderSales {
        val produtos = listOf(Produto("PAC0000107",100.0,1.0))
        val pedido = PedidoVenda("CLI0000001","2","BB-RC-BOL-1199",
                "-1",produtos)
        return save(pedido)
    }
}
