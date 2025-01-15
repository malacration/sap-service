package br.andrew.sap.controllers


import br.andrew.sap.controllers.documents.QuotationsController
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.CreditNotes
import br.andrew.sap.model.sap.documents.DownPaymentUnsetVendaFutura
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.ItemTroca
import br.andrew.sap.model.self.vendafutura.PedidoTroca
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchResponse
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Role
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal


@RestController
@RequestMapping("contrato-venda-futura")
class ContratoVendaFuturaController(
    val service : ContratoVendaFuturaService,
    val pedidoService : OrdersService,
    val itemService: ItemsService,
    val comissaoService: ComissaoService,
    val adiantamentoService : DownPaymentService,
    val creditNoteService: CreditNotesService,
    val batchService: BatchService,
    @Value("\${venda-futura.entrega:9}") val utilizacaoEntregaVendaFutura : Int,
    val cotacaoController : QuotationsController){
    val logger = LoggerFactory.getLogger(ContratoVendaFuturaController::class.java)

    @GetMapping("")
    fun get(auth : Authentication, page : Pageable): ResponseEntity<Page<Contrato>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        val contratos = service.get(Filter("U_vendedor",auth.getIdInt(),Condicao.EQUAL),
            OrderBy(mapOf("U_dataCriacao" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<Contrato>(page)
        return ResponseEntity.ok(contratos)
    }

    @GetMapping("/{id}")
    //TODO fazer metodo de validacao de acesso, admin ou o vendedor
    fun get(@PathVariable id : String, auth : Authentication): ResponseEntity<Contrato> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(service.getById(id).tryGetValue<Contrato>())
    }

    @GetMapping("all")
    fun getAll(auth : Authentication, page : Pageable): ResponseEntity<Page<Contrato>> {
        val contratos = service.get(Filter(),
            OrderBy(mapOf("U_dataCriacao" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<Contrato>(page)
        return ResponseEntity.ok(contratos)
    }

    @PostMapping("pedido-retirada")
    fun pedidoRetirada(@RequestBody pedidoRetirada : PedidoRetirada, auth : Authentication) : ResponseEntity<Document?> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val contrato = service.get(Filter(
            Predicate("DocEntry",pedidoRetirada.docEntryVendaFutura,Condicao.EQUAL)
        )).tryGetValues<Contrato>().firstOrNull() ?: throw  Exception("O contrato nao foi encontrado")
        val cotacao = pedidoRetirada.parse(contrato,utilizacaoEntregaVendaFutura)
        return ResponseEntity.ok(cotacaoController.saveForAngular(cotacao,auth))
    }

    @PostMapping("troca")
    fun troca(@RequestBody pedidoTroca : PedidoTroca, auth : Authentication): List<BatchResponse> {
        val bathcList = BatchList()
        val contrato = service
            .getById(pedidoTroca.docEntry)
            .tryGetValue<Contrato>()

        val entregas = this.pedidoService.getByContrato(contrato)
        if(entregas.size > 0)
            throw Exception("Existe entregas programadas para esse contrato, cancele elas para realizar a troca!")

        val resultado = contrato.troca(pedidoTroca,itemService,comissaoService)
        bathcList.add(BatchMethod.PUT,contrato,service)

        if(resultado.compareTo(BigDecimal.ZERO) > 0.0){
            val adiantamento = service.adiantamentoComplementarVendaFuturaWithoutSave(contrato,resultado)
            bathcList.add(BatchMethod.POST,adiantamento,adiantamentoService)
        }else if(resultado.compareTo(BigDecimal.ZERO) < 0.0){
            val resultadoModulo = resultado.multiply(BigDecimal(-1))
            val adiantamentoCancelar = service.adiantamentosAhCancelar(contrato,resultadoModulo)
            val valorResidual = adiantamentoCancelar.sumOf { BigDecimal(it.total()) }.minus(resultadoModulo)
            if(valorResidual.compareTo(BigDecimal.ZERO) < 0)
                throw Exception("Erro na Troca de Produto: Valor insuficiente nos boletos pendentes para ajustar o pagamento.")

            adiantamentoCancelar.forEach {
                bathcList.add(BatchMethod.PATCH, DownPaymentUnsetVendaFutura(it),adiantamentoService)
                bathcList.add(BatchMethod.POST,CreditNotes(it),creditNoteService)
            }

            if(valorResidual.compareTo(BigDecimal.ZERO) > 0){
                val dataVencimento = adiantamentoCancelar.map { it.calcularDataDeVencimento() }.minBy { it }
                val adiantamentoComplementar = adiantamentoService.adiantamentosVendaFuturaWithoutSave(contrato,
                    PaymentDueDates(valorResidual,dataVencimento.toLocalDate())
                )
                bathcList.add(BatchMethod.POST,adiantamentoComplementar,adiantamentoService)
            }
        }
        logger.info("Enviando ao sap o pedido de troca para o contrato ${contrato.getId()}")
        return batchService.run(bathcList)
    }
}
