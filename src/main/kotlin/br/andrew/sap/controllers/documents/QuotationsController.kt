package br.andrew.sap.controllers.documents

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Quotation
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.services.*
import br.andrew.sap.services.document.QuotationsService
import br.andrew.sap.services.pricing.ComissaoService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("quotation")
class QuotationsController(val quotationsService: QuotationsService,
                           val itemService : ItemsService,
                           val comissaoService: ComissaoService,
                           val telegramService : TelegramRequestService,
                           val applicationEventPublisher: ApplicationEventPublisher) {

    val logger = LoggerFactory.getLogger(QuotationsController::class.java)

    @PostMapping("")
    fun save(@RequestBody cotacao : PedidoVenda): Any {
        val quotation = cotacao.getQuotation(itemService,comissaoService).also {
            it.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
            it.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
        }
        return quotationsService.save(quotation).tryGetValue<Document>().also {
            try{
                applicationEventPublisher.publishEvent(it)
            }catch (e : Exception){
                logger.error(e.message,e)
            }
        }
    }

    @PostMapping("angular")
    fun saveForAngular(@RequestBody pedido : Quotation): Any {

        //TODO pegar salesPersonCode do login do usuario
        //TODO u_pedido_update = "1"'
        pedido.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
        pedido.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
        pedido.atualizaPrecoBase(itemService)
        pedido.u_pedido_update = "1"
        pedido.salesPersonCode = 27
        telegramService.send("Criando pedido pelo portal cliente")
        return quotationsService.save(pedido).tryGetValue<Document>().also {
            try{
                applicationEventPublisher.publishEvent(it)
            }catch (e : Exception){
                logger.error(e.message,e)
            }
        }
    }


    @GetMapping("")
    fun get(): List<OrderSales> {
        return quotationsService.get(OrderBy(mapOf("DocEntry" to Order.DESC))).tryGetValues<OrderSales>()
    }
}
