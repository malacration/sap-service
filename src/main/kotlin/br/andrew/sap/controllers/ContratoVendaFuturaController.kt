package br.andrew.sap.controllers


import br.andrew.sap.controllers.documents.QuotationsController
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.CreditNotes
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DownPaymentUnsetVendaFutura
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import br.andrew.sap.model.self.vendafutura.PedidoTroca
import br.andrew.sap.model.self.vendafutura.Status
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.InternalReconciliationsService
import br.andrew.sap.services.RecomNum
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchResponse
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import io.swagger.v3.oas.annotations.Parameter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
    val invoiceService : InvoiceService,
    val creditNotesService: CreditNotesService,
    val comissaoService: ComissaoService,
    val adiantamentoService : DownPaymentService,
    val creditNoteService: CreditNotesService,
    val internalReconciliationsService: InternalReconciliationsService,
    val orderService : OrdersService,
    val batchService: BatchService,
    @Value("\${venda-futura.entrega:9}") val utilizacaoEntregaVendaFutura : Int,
    val cotacaoController : QuotationsController){
    val logger = LoggerFactory.getLogger(ContratoVendaFuturaController::class.java)

    @GetMapping("")
    fun get(
        auth : Authentication,
        @RequestParam(value = "status", defaultValue = "aberto") status : Status,
        @RequestParam(value = "idContrato", defaultValue = "-1") idContrato : Int,
        @RequestParam(value = "filial", defaultValue = "-1") filial : Int,
            ): ResponseEntity<NextLink<Contrato>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val resultado = service.getContratos(auth,status, idContrato, filial)?.tryGetNextValues<Contrato>()
        return ResponseEntity.ok(resultado)
    }

    @GetMapping("/{id}/produtos")
    fun getProdutos(@PathVariable id : Int, auth : Authentication): ResponseEntity<List<Item>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(
            service.getById(id).tryGetValue<Contrato>().itens
        )
    }

    @GetMapping("/{id}")
    //TODO fazer metodo de validacao de acesso, admin ou o vendedor
    fun get(@PathVariable id : String, auth : Authentication): ResponseEntity<Contrato> {
        if(auth !is User)
            return ResponseEntity.noContent().build()
        return ResponseEntity.ok(service.getById(id).tryGetValue<Contrato>())
    }

    @PostMapping("/nextlink")
    fun nextLink(@RequestBody link : String, auth : Authentication): ResponseEntity<NextLink<Contrato>> {
        val nextLinkDto =
        return ResponseEntity.ok(
            service.next(link).tryGetNextValues<Contrato>()
        )
    }

    @GetMapping("/entregas/{idContrato}")
    fun entregas(@PathVariable idContrato: Int): List<Document> {
        val filter = Filter(Predicate("U_venda_futura", idContrato, Condicao.EQUAL),
            Predicate("DownPaymentAmountSC", 0, Condicao.EQUAL))
        return listOf(creditNotesService,invoiceService)
            .map { it.getAll(Document::class.java,filter) }
            .flatMap { it }
            .filter{ it.DocumentLines.none { it.BaseType == 203}}
            .sortedWith(compareBy(
                { it.docDate },
                { it.docObjectCode?.ordinal }
            ))
    }

    @PostMapping("pedido-retirada")
    fun pedidoRetirada(@RequestBody pedidoRetirada : PedidoRetirada, auth : Authentication) : ResponseEntity<Document?> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val contrato = service.get(Filter(
            Predicate("DocEntry",pedidoRetirada.docEntryVendaFutura,Condicao.EQUAL)
        )).tryGetValues<Contrato>().firstOrNull() ?: throw  Exception("O contrato nao foi encontrado")
        val orderSales = orderService.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()

        val cotacao = pedidoRetirada.parse(contrato,utilizacaoEntregaVendaFutura,null,orderSales)
        return ResponseEntity.ok(cotacaoController.saveForAngular(cotacao,auth))
    }

    @GetMapping("/encerrar/{docEntryVendaFutura}")
    fun encerrar(@PathVariable docEntryVendaFutura : Int) {
        val docMarketing = Filter(
            Predicate("U_venda_futura",docEntryVendaFutura,Condicao.EQUAL),
            Predicate("DocumentStatus",DocumentStatus.bost_Open,Condicao.EQUAL)
        )
        val contrato = service.getById(docEntryVendaFutura).tryGetValue<Contrato>()

        val notas = invoiceService.get(docMarketing).tryGetValues<Invoice>()
        if(notas.size > 0)
            throw Exception("Existe entregas que não foram conciliadas.")

        val boletosEmAberto = adiantamentoService.getByContratoVendaFutura(docEntryVendaFutura)
            .filter { it.DocumentStatus == DocumentStatus.bost_Open }
        if(boletosEmAberto.size > 0)
            throw Exception("Existe boletos em abertos")

        val adiantamentosPagos = adiantamentoService.adiantamentosAbertos(contrato.U_cardCode,docEntryVendaFutura)
        if(adiantamentosPagos.size > 0)
            throw Exception("Existe adiantamento pagos e não utilizado")

        service.update("{\"U_status\": \"cancelado\"}",docEntryVendaFutura.toString())
    }


    @GetMapping("/devolver/{docEntry}")
    fun devolver(@PathVariable docEntry : Int): List<RecomNum> {
        val recomNums = internalReconciliationsService.reconciliacaoByDocument(docEntry,13)

        if(recomNums.isEmpty())
            throw Exception("Nao existe conciliacao para o documento, proceda com a devolucao no SAP! [$docEntry]")
        if(recomNums.size != 1)
            throw Exception("Nao pode existir mais de uma reconciliacao para esse documento! [$docEntry]")
        invoiceService.update("{ \"U_conciliar_automatico\" : \"0\"}",docEntry.toString())
        internalReconciliationsService
            .serviceCancel("{ \"InternalReconciliationParams\": { \"ReconNum\": \"${recomNums.first().ReconNum}\"} }")
        return recomNums
    }

    @GetMapping("/emitir-boletos/{docEntry}")
    fun emitirBoleto(@PathVariable docEntry : Int){
        val contrato = service.getById(docEntry).tryGetValue<Contrato>()
        adiantamentoService.createAdiantamentoBycontrato(contrato,1)
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
