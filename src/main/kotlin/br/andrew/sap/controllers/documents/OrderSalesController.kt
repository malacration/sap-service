package br.andrew.sap.controllers.documents

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.dto.PixGeradoResponse
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.model.dto.OrderSalesLineItem
import br.andrew.sap.model.dto.OrderSalesListItem
import br.andrew.sap.model.sap.Localidade
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.*
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.document.DocumentForAngular
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.stock.ItemsService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("pedido-venda")
class OrderSalesController(val ordersService: OrdersService,
                           val itemService : ItemsService,
                           private val downPaymentService: DownPaymentService,
                           val comissaoService: ComissaoService,
                           val telegramService : TelegramRequestService,
                           val applicationEventPublisher: ApplicationEventPublisher,
                           val sqlQueriesService : SqlQueriesService
) {

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
    fun listar(auth: Authentication,
               @RequestParam status: DocumentStatus?,
               @RequestParam filial: Int?,
               @RequestParam cliente: String?,
               @RequestParam data: String?
    ): ResponseEntity<NextLink<OrderSalesListItem>> {
        if (auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(ordersService.listar(auth, status, filial, cliente, data))
    }

    @GetMapping("{docEntry}/linhas")
    fun listarLinhas(@PathVariable docEntry: Int): List<OrderSalesLineItem> {
        return ordersService.listarLinhas(docEntry)
    }

    @PostMapping("listar/nextlink")
    fun listarNextLink(@RequestBody nextLink: String): NextLink<OrderSalesListItem> {
        return ordersService.listarNextLink(nextLink)
    }

    @GetMapping("pix/{docEntry}")
    fun getAdiantamentoPixByOrder(@PathVariable docEntry : Int, page : Pageable) : Page<PixGeradoResponse> {
        val filterPixPedido = Filter(
            Predicate("U_TX_DocEntryRef",docEntry, Condicao.EQUAL),
        )
        val pageResult = downPaymentService.get(filterPixPedido, OrderBy("DocEntry",Order.DESC))
            .tryGetPageValues<Document>(page)
        val conteudo = pageResult.content
            .flatMap{ document -> (document.documentInstallments ?: emptyList()).map { Pair(document, it) }   }
            .mapNotNull { par ->  PixGeradoResponse(par.second,0.0).also {
                it.docEntry = par.first.docEntry
                it.docNum = par.first.docNum
                it.status = par.first.DocumentStatus
            } }

        return PageImpl(
            conteudo,
            pageResult.pageable,
            conteudo.size.toLong()
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

    @PostMapping("/searchAll")
    fun search(@RequestBody nextLink : String): NextLink<OrderSales> {
        val result = ordersService.fullSearchTextFallBack2(nextLink)
        return result ?: NextLink(emptyList(), "")
    }

    @GetMapping("/pedido/all/{docEntry}/{order2}/order-id")
    fun devolver(@PathVariable docEntry : Int, @PathVariable order2 : Int) {
        val order = ordersService.getById(docEntry).tryGetValue<OrderSales>()

        val lines = order.DocumentLines.map {
            "{ \"DocEntry\": ${it.DocEntry}, \"LineNum\": ${it.LineNum},  \"U_ORD_CARREGAMENTO\" : \"${order2}\" }"
        }
        val json = "{\"DocumentLines\": [" +
                lines.joinToString(",") +
                "]}"

        ordersService.update(json,docEntry.toString())
    }


    @GetMapping("/findLoadOrders")
    fun findLoadOrders(@RequestParam("U_Ordem_Carregamento") U_Ordem_Carregamento: String): NextLink<OrderSales> {
        val result = ordersService.FindLoadOrders(U_Ordem_Carregamento)
        return result ?: NextLink(emptyList(), "")
    }

    @PostMapping("/findAllLoadOrders")
    fun findAllLoadOrders(@RequestBody nextLink: String): NextLink<OrderSales> {
        val result = sqlQueriesService.nextLink(nextLink)!!.tryGetNextValues<OrderSales>()
        return result ?: NextLink(emptyList(), "")
    }

    @GetMapping("/searchLocality")
    fun searchLocalidade(@RequestParam("Code") Code: Int): NextLink<Localidade> {
        val result = ordersService.SearchLocality(Code)
        return result ?: NextLink(emptyList(), "")
    }
}
