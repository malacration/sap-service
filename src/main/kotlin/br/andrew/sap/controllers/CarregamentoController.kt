package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.model.sap.Carregamento
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.*
import br.andrew.sap.services.document.DocumentForAngular
import br.andrew.sap.services.document.QuotationsService
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
@RequestMapping("carregamento")
class CarregamentoController(val carregamentoServico: CarregamentoService,
                             val itemService : ItemsService,
                             val comissaoService: ComissaoService,
                             val telegramService : TelegramRequestService,
                             val applicationEventPublisher: ApplicationEventPublisher) {

    val logger = LoggerFactory.getLogger(QuotationsController::class.java)

    @GetMapping("todos")
    fun getFazendas() : OData{
        return carregamentoServico.get()
    }

    @GetMapping("todos2")
    fun getFazendas2() : List<Carregamento>{
        return carregamentoServico.get().tryGetValues<Carregamento>()
    }

    @GetMapping("")
    fun get(auth : Authentication, page : Pageable): ResponseEntity<Page<Carregamento>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        val carregamento = carregamentoServico.get(Filter(),
            OrderBy(mapOf("CreateDate" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<Carregamento>(page)
        return ResponseEntity.ok(carregamento)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id : String, auth : Authentication): ResponseEntity<Carregamento> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(carregamentoServico.getById(id).tryGetValue<Carregamento>())
    }


    @GetMapping("all")
    fun getAll(auth : Authentication, page : Pageable): ResponseEntity<Page<Carregamento>> {
        val carregamento = carregamentoServico.get(Filter(),
            OrderBy(mapOf("CreateDate" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<Carregamento>(page)
        return ResponseEntity.ok(carregamento)
    }
}
