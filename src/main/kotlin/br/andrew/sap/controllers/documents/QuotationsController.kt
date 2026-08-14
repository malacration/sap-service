package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.dto.AutorizacaoPendenteDto
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.model.sistema.TipoDocumentoAutorizacao
import br.andrew.sap.services.*
import br.andrew.sap.services.autorizacao.AutorizacaoService
import br.andrew.sap.services.autorizacao.RegraAutorizacaoService
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.documents.DocumentForAngular
import br.andrew.sap.services.documents.QuotationsService
import br.andrew.sap.services.logistica.RegiaoService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.stock.ItemsService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import br.andrew.sap.services.integracao.TelegramRequestService

@RestController
@RequestMapping("quotation")
class QuotationsController(val quotationsService: QuotationsService,
                           val itemService : ItemsService,
                           val comissaoService: ComissaoService,
                           val telegramService : TelegramRequestService,
                           val applicationEventPublisher: ApplicationEventPublisher,
                           val businessPartnersService : BusinessPartnersService,
                           val regiaoService : RegiaoService,
                           val regraAutorizacaoService: RegraAutorizacaoService,
                           val autorizacaoService: AutorizacaoService) {

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
    fun saveForAngular(@RequestBody pedido : Quotation, auth : Authentication): ResponseEntity<Any> {
        val document = DocumentForAngular().prepareToSave(pedido,itemService,businessPartnersService,regiaoService,auth)
        val motivo = regraAutorizacaoService.avaliar(document)
        if(motivo != null){
            val autorizacao = autorizacaoService.criar(
                TipoDocumentoAutorizacao.COTACAO, motivo, document, (auth as User).userName)
            return ResponseEntity.accepted().body(AutorizacaoPendenteDto(autorizacao.Code, motivo))
        }
        telegramService.send("Criando pedido pelo portal cliente")
        return ResponseEntity.ok(quotationsService.save(document).tryGetValue<Document>().also {
            try{
                applicationEventPublisher.publishEvent(it)
            }catch (e : Exception){
                logger.error(e.message,e)
            }
        })
    }

    @GetMapping("")
    fun get(page : Pageable, auth : Authentication): ResponseEntity<Page<OrderSales>> {
        if(!(auth is User))
            return ResponseEntity.noContent().build()
        val predicados = mutableListOf<Predicate>(
            Predicate("SalesPersonCode",
                auth.getIdInt(),
                Condicao.EQUAL)
        )
        return ResponseEntity.ok(quotationsService
                .get(Filter(predicados), OrderBy(mapOf("DocEntry" to Order.DESC)), page)
                .tryGetPageValues<OrderSales>(page)
            )

    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String): OrderSales {
        return quotationsService.getById(id).tryGetValue<OrderSales>()
    }
}
