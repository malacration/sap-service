package br.andrew.sap.controllers.documents

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.*
import br.andrew.sap.services.document.DocumentForAngular
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.stock.ItemsService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService,
                           val itemService : ItemsService,
                           val comissaoService: ComissaoService,
                           val telegramService : TelegramRequestService,
                           val applicationEventPublisher: ApplicationEventPublisher) {

    val logger = LoggerFactory.getLogger(OrderSalesController::class.java)

    @PostMapping("")
    fun saveForSovis(@RequestBody pedido : PedidoVenda): Any {
        val order = pedido.getOrder(itemService,comissaoService).also {
            it.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
            it.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
        }
        try {
            val retorno = ordersService.save(order).tryGetValue<OrderSales>()
            applicationEventPublisher.publishEvent(OrderSalesSaveEvent(retorno))
            return retorno;
        }catch (t : CreditException){
            logger.warn(t.message,t)
            return t.getDocumentFake(order).also { applicationEventPublisher.publishEvent(t) }
        }
    }

    @GetMapping("")
    fun get(): List<OrderSales> {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC))).tryGetValues<OrderSales>()
    }


    @GetMapping("raw")
    fun raw(): OData {
        return ordersService.get(OrderBy(mapOf("DocEntry" to Order.DESC)))
    }

    @GetMapping("raw/{id}")
    fun rawById(@PathVariable id : String): OData {
        return ordersService.getById(id)
    }

    @GetMapping("listar")
    fun get(page : Pageable, auth : Authentication): ResponseEntity<Page<OrderSales>> {
        if(!(auth is User))
            return ResponseEntity.noContent().build()
        val predicados = mutableListOf<Predicate>(
            Predicate("SalesPersonCode",
                auth.getIdInt(),
                Condicao.EQUAL)
        )
        return ResponseEntity.ok(ordersService
            .get(Filter(predicados), OrderBy(mapOf("DocEntry" to Order.DESC)), page)
            .tryGetPageValues<OrderSales>(page)
        )
    }

    @PostMapping("angular")
    fun saveForAngular(@RequestBody pedido : OrderSales, auth : Authentication): Document {
        val document = DocumentForAngular().prepareToSave(pedido,itemService,auth)
        telegramService.send("Criando pedido pelo portal cliente")
        return ordersService.save(document).tryGetValue<Document>().also {
            try{
                applicationEventPublisher.publishEvent(it)
            }catch (e : Exception){
                logger.error(e.message,e)
            }
        }
    }

    @PostMapping("search")
    fun search(@RequestBody filter: OrderSalesFilter): NextLink<OrderSales> {
        val teste = ordersService.fullSearch(filter)
            return teste
        }

    data class OrderSalesFilter(
        val dataInicial: String,
        val dataFinal: String,
        val localidade: String,
        val filial: String,
        val skip: Int?
    )
}
