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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService,
                           val businesPartner : BusinessPartnersService) {


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
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class DocumentWrapper(val Document : OrderSales

)