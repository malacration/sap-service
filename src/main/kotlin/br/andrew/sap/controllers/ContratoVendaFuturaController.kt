package br.andrew.sap.controllers


import br.andrew.sap.controllers.documents.QuotationsController
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.Version
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.entity.ChildTables
import br.andrew.sap.model.entity.FormColumns
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.sap.Branch
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.TelegramRequestService
import br.andrew.sap.services.abstracts.TesteService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.document.QuotationsService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("contrato-venda-futura")
class ContratoVendaFuturaController(
    val service : ContratoVendaFuturaService,
    val pedidoService : OrdersService,
    val cotacaoController : QuotationsController){

    val logger = LoggerFactory.getLogger(ContratoVendaFuturaController::class.java)

    @GetMapping("")
    fun get(auth : Authentication, page : Pageable): ResponseEntity<Page<Contrato>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        val contratos = service.get(Filter("U_vendedor",auth.getIdInt(),Condicao.EQUAL),page)
            .tryGetPageValues<Contrato>(page)
        return ResponseEntity.ok(contratos)
    }

    @PostMapping("pedido-retirada")
    fun teste(@RequestBody pedidoRetirada : PedidoRetirada, auth : Authentication) : ResponseEntity<Document?> {
        //TODO fazer parametro desse valor
        val controlAccount = "2.1.2.001.00005"
        val utilizacao = 9
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val contratos = service.get(Filter(
            Predicate("DocEntry",pedidoRetirada.docEntryVendaFutura,Condicao.EQUAL)
        )).tryGetValues<Contrato>()

        if(contratos.size != 1)
            throw Exception("O contrato nao foi encontrado")
        val contrato = contratos.firstOrNull() ?: throw Exception("Erro ao consultar contrato")
        val baseDocument = pedidoService.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()
        val documento = baseDocument.getQuotationVendaFutura(pedidoRetirada,controlAccount,utilizacao)
        return ResponseEntity.ok(cotacaoController.saveForAngular(documento,auth))
    }
}
