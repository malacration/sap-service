package br.andrew.sap.controllers.documents

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.services.*
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService,
                           val itemService : ItemsService,
                           val comissaoService: ComissaoService,
                           val applicationEventPublisher: ApplicationEventPublisher) {

    val logger = LoggerFactory.getLogger(OrderSalesController::class.java)

    @PostMapping("")
    fun save(@RequestBody pedido : PedidoVenda): Any {
        val order = pedido.getOrder(itemService,comissaoService).also {
            it.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
            it.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
        }
        try {
            return ordersService.save(order).tryGetValue<OrderSales>().also {
                applicationEventPublisher.publishEvent(OrderSalesSaveEvent(order))
            }
        }catch (t : CreditException){
            logger.warn(t.message,t)
            return t.getOrderFake(order).also { applicationEventPublisher.publishEvent(t) }
        }
    }

    @GetMapping("")
    fun get(): List<OrderSales> {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC))).tryGetValues<OrderSales>()
    }
}
