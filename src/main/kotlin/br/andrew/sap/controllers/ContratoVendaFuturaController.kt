package br.andrew.sap.controllers


import br.andrew.sap.controllers.documents.QuotationsController
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DownPaymentUnsetVendaFutura
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import br.andrew.sap.model.self.vendafutura.PedidoTroca
import br.andrew.sap.model.self.vendafutura.Status
import br.andrew.sap.model.self.vendafutura.VincularDevolucao
import br.andrew.sap.model.sap.InternalReconciliationsBuilder
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.InternalReconciliationsService
import br.andrew.sap.services.RecomNum
import br.andrew.sap.services.futura.EstornoReclassificacaoVendaFuturaService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.model.transaction.TransactionCodeTypes
import JournalEntry
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchResponse
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.model.impostos.ImpostosDesonerados
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.math.RoundingMode


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
    val estornoReclassificacaoService : EstornoReclassificacaoVendaFuturaService,
    val journalEntriesService : JournalEntriesService,
    @Value("\${venda-futura.entrega:9}") val utilizacaoEntregaVendaFutura : Int,
    val cotacaoController : QuotationsController,
    val impostosDesonerados : ImpostosDesonerados){
    val logger = LoggerFactory.getLogger(ContratoVendaFuturaController::class.java)

    @GetMapping("")
    fun get(
        auth : Authentication,
        @RequestParam(value = "status", defaultValue = "aberto") status : Status,
        @RequestParam(value = "idContrato", defaultValue = "-1") idContrato : Int,
        @RequestParam(value = "filial", defaultValue = "-1") filial : Int,
        @RequestParam(value = "cliente", defaultValue = "-1") cliente : String,
            ): ResponseEntity<NextLink<Contrato>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val resultado = service.getContratos(auth, status, idContrato, filial, cliente)?.tryGetNextValues<Contrato>()
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
        val filter = Filter(
            Predicate("Cancelled", Cancelled.tNO, Condicao.EQUAL),
            Predicate("U_venda_futura", idContrato, Condicao.EQUAL),
            Predicate("DownPaymentAmountSC", 0, Condicao.EQUAL))
        return listOf(creditNotesService,invoiceService)
            .map { it.getAll(Document::class.java,filter) }
            .flatMap { it }
            .filter{ it.DocumentLines.none { it.BaseType == 203}}
            .onEach { it.preencheDesonerado(impostosDesonerados.ids) }
            .sortedWith(compareBy(
                { it.docDate },
                { it.docObjectCode?.ordinal }
            ))
    }

    @PostMapping("pedido-retirada")
    fun pedidoRetirada(@RequestBody pedidoRetirada : PedidoRetirada, auth : Authentication) : ResponseEntity<Document?> {
        val contrato = service.get(Filter(
            Predicate("DocEntry",pedidoRetirada.docEntryVendaFutura,Condicao.EQUAL)
        )).tryGetValues<Contrato>().firstOrNull() ?: throw  Exception("O contrato nao foi encontrado")
        val boletos = adiantamentoService.getByContratoVendaFutura(contrato.DocEntry!!)
        if (boletos.isEmpty())
            throw Exception("Não existem adiantamentos criados para o contrato ${contrato.DocEntry}. Emita os boletos antes de realizar a retirada.")
        val boleto = boletos.last()
        val numerosBoletos = adiantamentoService.getOurNumbersByContratoVendaFutura(contrato.DocEntry!!)
        val orderSales = orderService.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()
        val cotacao = pedidoRetirada.parse(contrato,utilizacaoEntregaVendaFutura,boleto.DocDueDate,orderSales,numerosBoletos)
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
        invoiceService.update("{ \"U_conciliar_automatico\" : \"0\", \"U_vf_estornada\" : \"1\"}",docEntry.toString())
        internalReconciliationsService
            .serviceCancel("{ \"InternalReconciliationParams\": { \"ReconNum\": \"${recomNums.first().ReconNum}\"} }")
        return recomNums
    }

    /**
     * Vincula uma devolução avulsa (nota de crédito lançada solta no SAP B1) a um contrato de
     * venda futura e executa, de forma síncrona, todo o acerto financeiro:
     * cancela a conciliação da reclassificação da nota de saída, concilia o cliente
     * (nota de saída × devolução) e faz a reversão da reclassificação (VFDV + estorno de
     * adiantamento). Ver plano em `venda-futura`.
     */
    @PostMapping("/{docEntryContrato}/vincular-devolucao")
    fun vincularDevolucao(@PathVariable docEntryContrato : Int, @RequestBody body : VincularDevolucao) {
        val contrato = service.getById(docEntryContrato).tryGetValue<Contrato>()
        val devolucao = creditNotesService.get(Filter(
            Predicate("DocNum", body.devolucaoDocNum, Condicao.EQUAL),
            Predicate("Cancelled", Cancelled.tNO, Condicao.EQUAL)
        )).tryGetValues<Invoice>().firstOrNull()
            ?: throw Exception("Devolução com DocNum ${body.devolucaoDocNum} não encontrada")
        val notaSaida = invoiceService.getById(body.notaSaidaDocEntry).tryGetValue<Invoice>()

        // ---- Validações (nenhuma postagem é feita antes de todas passarem) ----
        if(devolucao.CardCode != contrato.U_cardCode || notaSaida.CardCode != contrato.U_cardCode)
            throw Exception("A devolução e/ou a nota de saída não são do mesmo cliente do contrato")
        if(devolucao.getBPL_IDAssignedToInvoice() != notaSaida.getBPL_IDAssignedToInvoice())
            throw Exception("A devolução e a nota de saída são de filiais diferentes")
        if(notaSaida.U_venda_futura != contrato.DocEntry)
            throw Exception("A nota de saída informada não pertence a este contrato")

        val itensContrato = contrato.itens.map { it.U_itemCode }.toSet()
        val itensFora = devolucao.DocumentLines.mapNotNull { it.ItemCode }.filter { !itensContrato.contains(it) }
        if(itensFora.isNotEmpty())
            throw Exception("A devolução contém itens que não estão no contrato: ${itensFora.joinToString()}")

        val totalDev = BigDecimal(devolucao.DocTotal ?: throw Exception("Devolução sem total"))
            .setScale(2, RoundingMode.HALF_UP)
        val totalNota = BigDecimal(notaSaida.DocTotal ?: throw Exception("Nota de saída sem total"))
            .setScale(2, RoundingMode.HALF_UP)
        if(totalDev.compareTo(totalNota) != 0)
            throw Exception("O total da devolução ($totalDev) não bate com a nota de saída informada ($totalNota)")

        // Estado da nota: só aberta (sem conciliação) ou conciliada com reclassificação (30).
        // Se já estiver conciliada com uma devolução (14), é provável cancelamento em duplicidade.
        val contrapartidas = internalReconciliationsService
            .contrapartidasReconciliacao(notaSaida.docEntry ?: -1, DocumentTypes.oInvoices.value)
        if(contrapartidas.contains(DocumentTypes.oCreditNotes.value))
            throw Exception("A nota de saída já está conciliada com uma devolução (provável cancelamento em duplicidade)")

        // Reclassificação já apropriada (adiantamento consumido, transcode VFEC): cenário mais
        // complexo, tratado à parte. Barra antes de qualquer lançamento para não sujar a contabilidade.
        if(estornoReclassificacaoService.apropriacoes(listOf(notaSaida.docEntry ?: -1)).isNotEmpty())
            throw Exception("A reclassificação desta nota já teve o adiantamento apropriado; " +
                "faça o acerto dessa devolução manualmente no SAP por enquanto")

        // ---- Execução ----
        // 3. Vincular a devolução ao contrato
        creditNotesService.update("{\"U_venda_futura\": ${contrato.DocEntry}}", devolucao.docEntry.toString())

        // 4. Cancelar a conciliação da reclassificação (reabre a nota; não apaga o lançamento)
        internalReconciliationsService.reconciliacaoByDocument(notaSaida.docEntry ?: -1, DocumentTypes.oInvoices.value)
            .forEach {
                internalReconciliationsService
                    .serviceCancel("{ \"InternalReconciliationParams\": { \"ReconNum\": \"${it.ReconNum}\"} }")
            }

        // Rebusca por DocEntry (GET único traz o documento completo) já no estado atual:
        // nota reaberta pelo passo 4 e devolução vinculada pelo passo 3. A devolução pode não
        // ter parcelas — nesse caso a reconciliação usa o documento inteiro (ver Document.getReconciliationRows).
        val notaSaidaConciliar = invoiceService.getById(notaSaida.docEntry ?: -1).tryGetValue<Invoice>()
        val devolucaoConciliar = creditNotesService.getById(devolucao.docEntry ?: -1).tryGetValue<Invoice>()

        // 5. Conciliar o cliente (nota de saída × devolução)
        internalReconciliationsService.save(InternalReconciliationsBuilder(notaSaidaConciliar, devolucaoConciliar).build())

        // 6. Reverter a reclassificação (VFDV) + estorno do adiantamento
        val vfdv = estornoReclassificacaoService.estornar(devolucaoConciliar, listOf(notaSaida.docEntry ?: -1))

        // 6.1. Conciliar a reclassificação inicial (VFET/VFEC da nota) com o estorno (VFDV).
        // O passo 4 reabriu a reclassificação e o VFDV está em aberto na mesma conta controle;
        // aqui os dois se fecham entre si.
        val reclassificacao = journalEntriesService
            .getAll(JournalEntry::class.java, Filter(Predicate("Reference", notaSaida.docNum ?: "", Condicao.EQUAL)))
            .firstOrNull {
                it.TransactionCode == TransactionCodeTypes.VFET.toString() ||
                it.TransactionCode == TransactionCodeTypes.VFEC.toString()
            } ?: throw Exception("Reclassificação inicial (VFET/VFEC) da nota ${notaSaida.docNum} não encontrada")
        internalReconciliationsService.save(InternalReconciliationsBuilder(reclassificacao, vfdv).build())

        // 7. Fechar o flag para o schedule estorno() não reprocessar e ocultar a nota na UI
        // (não pode mais ser estornada/selecionada, pois já recebeu esta devolução).
        creditNotesService.update("{\"U_conciliar_automatico\": \"0\"}", devolucao.docEntry.toString())
        invoiceService.update("{\"U_vf_estornada\": \"1\"}", notaSaida.docEntry.toString())
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
                bathcList.add(BatchMethod.POST,adiantamentoService.devolucaoAdiantamentoVendaFutura(it),creditNoteService)
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
