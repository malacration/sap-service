package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.infrastructure.websocket.SessionProcessingRegistry
import br.andrew.sap.model.CodeRange
import br.andrew.sap.model.StartProcessRequest
import br.andrew.sap.model.calculadora.*
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.ProductTreesService
import br.andrew.sap.services.calculadora.CalculadoraHanddleService
import br.andrew.sap.services.calculadora.CalculadoraService
import br.andrew.sap.services.stock.ResourceService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.security.Principal
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("calculadora-preco")
class CalculadoraPrecoController(
    val service : CalculadoraService,
    val itemService: ItemsService,
    val calculadoraHandle : CalculadoraHanddleService,
    val productTreesService: ProductTreesService,
    val resourceService : ResourceService,
    private val ws: SimpMessagingTemplate,
    private val sessionRegistry: SessionProcessingRegistry,
    @Value("\${ggf.code:GGF00001}") val ggfId : String,
) {

    @MessageMapping("/calculadora-preco/get-async")
    fun start(
        req: StartProcessRequest<CodeRange>,
        principal: Principal,
        headerAccessor: SimpMessageHeaderAccessor) {

        val sessionId = headerAccessor.sessionId
        val cancelToken = sessionId?.let { sessionRegistry.register(it) }

        CompletableFuture.runAsync {
            try {
                val ggf = resourceService.getById(ggfId).tryGetValue<Resource>()
                var itensComEstrutura = itemService.produtosComEstrutura("").map { it.ItemCode }

                val filter = Filter(
                    Predicate("ItemCode",req.params.codeStart, Condicao.GREAT_EQUAL),
                    Predicate("ItemCode",req.params.codeEnd, Condicao.LESS_EQUAL),
                    Predicate("Valid", YesNo.tYES, Condicao.EQUAL)
                )

                var page: Pageable = PageRequest.of(0, 20)
                val ha = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
                ha.sessionId = headerAccessor.sessionId
                ha.setLeaveMutable(true)

                do {
                    if (cancelToken?.get() == true) {
                        break
                    }
                    val resultado = itemService.get(filter, page)
                        .tryGetPageValues<Produto>(page)
                    val resultCompleto = resultado.content.filter { itensComEstrutura.contains(it.ItemCode) }
                        .also {
                            it.forEach{
                                it.defaultWareHouse = req.params.warehouse
                                it.kgsPorUnidade = try { calculadoraHandle.getKgsPorUnidade(it) }catch (e : Exception) { BigDecimal(1.0) }
                                it.custoGgf = ggf.sumCost()
                                it.ingredientes = calculadoraHandle.getBasicIngredientes(it.ItemCode,itensComEstrutura,BigDecimal("1.0"),
                                    req.params.warehouse ?: throw Exception("Erro warehouse null")
                                )
                            }}
                    println("Enviando mensagem no ws - $resultado")
                    ws.convertAndSendToUser(principal.name, "/queue/calculadora-preco/get-async", resultCompleto, ha.messageHeaders)
                    page = resultado.nextPageable()
                }while (!resultado.isLast)
            } finally {
                if (sessionId != null && cancelToken != null) {
                    sessionRegistry.clear(sessionId, cancelToken)
                }
            }
        }
    }

    @GetMapping("/itens/all")
    fun get(
        @RequestParam(name = "itemCodeStart") start: String,
        @RequestParam(name = "itemCodeEnd") end: String,
        @RequestParam(name = "warehouse") warehouse: String): List<Produto> {

        val ggf = resourceService.getById(ggfId).tryGetValue<Resource>()
        var itensComEstrutura = itemService.produtosComEstrutura("").map { it.ItemCode }

        val filter = Filter(
            Predicate("ItemCode",start, Condicao.GREAT_EQUAL),
            Predicate("ItemCode",end, Condicao.LESS_EQUAL),
            Predicate("Valid", YesNo.tYES, Condicao.EQUAL)
        )

        return itemService.getAll(Produto::class.java,filter)
            .filter { itensComEstrutura.contains(it.ItemCode) }
            .also {
                it.forEach{
                    it.defaultWareHouse = warehouse
                    it.kgsPorUnidade = try { calculadoraHandle.getKgsPorUnidade(it) }catch (e : Exception) { BigDecimal(1.0) }
                    it.custoGgf = ggf.sumCost()
                    it.ingredientes = calculadoraHandle.getBasicIngredientes(it.ItemCode,itensComEstrutura,BigDecimal("1.0"),warehouse)
            }}
    }

    @GetMapping("/product-trees-service/all")
    fun productTree(
        @RequestParam(name = "itemCodePrefix", defaultValue = "PAC") itemCodePrefix: String,
        page : Pageable,
        auth : Authentication): OData {
        val filter = Filter(
            Predicate("TreeCode",itemCodePrefix, Condicao.STARTS_WITH)
        )
        return productTreesService.get(filter)
    }

    @GetMapping("/last-price/{itemCode}/{deposito}")
    fun lastPrice(
        @PathVariable itemCode: String,
        @PathVariable deposito: String,
        page : Pageable,
        auth : Authentication): List<LastPrice> {
        return itemService.getLastPrice(itemCode, deposito)
    }

    @GetMapping()
    fun get(): OData {
        return service.get()
    }

    @PostMapping
    fun save(@RequestBody entity : CalculadoraPreco): OData {
        return service.save(entity)
    }


}
