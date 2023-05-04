package br.andrew.sap.controllers

import br.andrew.sap.events.DraftOrderSalesSaveEvent
import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.exceptions.LinkedPaymentMethodException
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.services.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
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

    @GetMapping("data")
    fun getData(): OData {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC)))
    }


}
