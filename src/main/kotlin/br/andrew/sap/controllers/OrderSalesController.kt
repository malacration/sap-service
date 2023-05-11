package br.andrew.sap.controllers

import br.andrew.sap.events.DraftOrderSalesSaveEvent
import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.exceptions.LinkedPaymentMethodException
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.services.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.RequestEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService,
                           val businesPartner : BusinessPartnersService,
                           val itemService : ItemsService,
                           val approvalRequest : ApprovalRequestsService,
                           val draft : DraftsService,
                           val applicationEventPublisher: ApplicationEventPublisher) {

    val logger = LoggerFactory.getLogger(OrderSalesController::class.java)

    @PostMapping("")
    fun save(@RequestBody pedido : PedidoVenda): Any {
        try {
            val order = ordersService.save(
                    pedido.getOrder().also {
                        it.aplicaBase(itemService)
                        it.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
                    }
            ).tryGetValue<OrderSales>()
            applicationEventPublisher.publishEvent(OrderSalesSaveEvent(order))
            return order
        }catch (t : LinkedPaymentMethodException){
            businesPartner.addPaymentMethod(pedido.idCliente,pedido.idFormaPagamento)
            throw t
        }catch (t : CreditException){
            logger.warn(t.message,t)
            return t.getOrderFake(pedido).also { applicationEventPublisher.publishEvent(DraftOrderSalesSaveEvent(it)) }
        }
    }

    @GetMapping("")
    fun get(): List<OrderSales> {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC))).tryGetValues<OrderSales>()
    }

    @GetMapping("teste")
    fun getData(): Any {
        val json = "{\"dataEntraga\":\"2023-05-08\",\"observacao\":\"CLIENTE RETIRA.\",\"idCliente\":\"CLI0000297\",\"desconto\":0,\"produtos\":[{\"precoUnitario\":77.83,\"idProduto\":\"PAC0000069\",\"desconto\":8.0023641,\"valorTabela\":84.6,\"quantidade\":6},{\"precoUnitario\":93.53,\"idProduto\":\"PAC0000138\",\"desconto\":7.0093458,\"valorTabela\":100.58,\"quantidade\":10}],\"idCondicaoPagamento\":-2,\"frete\":0,\"idEmpresa\":11,\"tipoPedido\":9,\"codVendedor\":55,\"idPedido\":234,\"idFormaPagamento\":\"AVISTA\"}"
        return save(ObjectMapper().registerModule(KotlinModule()).readValue(json, jacksonTypeRef<PedidoVenda>()))
    }


}
