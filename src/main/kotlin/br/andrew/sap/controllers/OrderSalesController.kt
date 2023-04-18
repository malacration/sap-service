package br.andrew.sap.controllers

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.exceptions.LinkedPaymentMethodException
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.services.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService,
                           val businesPartner : BusinessPartnersService,
                           val itemService : ItemsService,
        val applicationEventPublisher: ApplicationEventPublisher) {

    @PostMapping("")
    fun save(@RequestBody pedido : PedidoVenda): OrderSales {
        try {
            val order = ordersService.save(
                    pedido.getOrder().also { it.aplicaBase(itemService) }
            ).tryGetValue<OrderSales>()
            applicationEventPublisher.publishEvent(OrderSalesSaveEvent(order))
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
        val json = "{\"dataEntraga\":\"2023-04-18\",\"observacao\":\"\",\"idCliente\":\"CLI0002765\",\"desconto\":0,\"produtos\":[{\"precoUnitario\":130.05,\"idProduto\":\"PAC0000098\",\"desconto\":0,\"valorTabela\":130.05,\"quantidade\":6},{\"precoUnitario\":132.6,\"idProduto\":\"PAC0000089\",\"desconto\":0,\"valorTabela\":132.6,\"quantidade\":4},{\"precoUnitario\":117.3,\"idProduto\":\"PAC0000075\",\"desconto\":0,\"valorTabela\":117.3,\"quantidade\":6}],\"idCondicaoPagamento\":24,\"frete\":50,\"idEmpresa\":2,\"tipoPedido\":16,\"codVendedor\":54,\"idPedido\":52,\"idFormaPagamento\":\"BB-RC-BOL-1199\"}"
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val pedido = mapper.readValue(json, jacksonTypeRef<PedidoVenda>())
        return this.save(pedido)
    }
}
