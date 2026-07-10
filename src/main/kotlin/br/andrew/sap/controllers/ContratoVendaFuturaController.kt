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
        val devolucaoResolvida = creditNotesService.get(Filter(
            Predicate("DocNum", body.devolucaoDocNum, Condicao.EQUAL),
            Predicate("Cancelled", Cancelled.tNO, Condicao.EQUAL)
        )).tryGetValues<Invoice>().firstOrNull()
            ?: throw Exception("Devolução com DocNum ${body.devolucaoDocNum} não encontrada")
        // GET único garante as linhas completas (BaseType/BaseEntry) para amarrar a devolução à nota.
        val devolucao = creditNotesService.getById(devolucaoResolvida.docEntry ?: -1).tryGetValue<Invoice>()
        val notaSaida = invoiceService.getById(body.notaSaidaDocEntry).tryGetValue<Invoice>()

        // ---- Validações (nenhuma postagem é feita antes de todas passarem) ----
        if(devolucao.CardCode != contrato.U_cardCode || notaSaida.CardCode != contrato.U_cardCode)
            throw Exception("A devolução e/ou a nota de saída não são do mesmo cliente do contrato")
        if(devolucao.getBPL_IDAssignedToInvoice() != notaSaida.getBPL_IDAssignedToInvoice())
            throw Exception("A devolução e a nota de saída são de filiais diferentes")
        if(notaSaida.U_venda_futura != contrato.DocEntry)
            throw Exception("A nota de saída informada não pertence a este contrato")

        // A devolução tem que ser avulsa (sem contrato) ou já ser deste contrato (reprocessamento).
        // Se já pertence a OUTRO contrato, o passo 3 a desvincularia dele — barra aqui.
        val vfDevolucao = devolucao.U_venda_futura
        if(vfDevolucao != null && vfDevolucao != 0 && vfDevolucao != contrato.DocEntry)
            throw Exception("A devolução ${devolucao.docNum} já está vinculada a outro contrato de venda futura ($vfDevolucao)")

        // A devolução tem que corresponder à NOTA selecionada — não apenas conter itens que
        // existem no contrato. Com duas entregas de mesmo item/total, checar só o contrato
        // permitiria vincular a devolução de uma entrega à outra. (A nota já foi validada como
        // pertencente ao contrato acima, então isto também garante itens dentro do contrato.)
        val basesDaDevolucao = devolucao.DocumentLines
            .filter { it.BaseType == DocumentTypes.oInvoices.value }
            .mapNotNull { it.BaseEntry }
            .toSet()
        if(basesDaDevolucao.isNotEmpty()) {
            // Devolução copiada de nota(s) de saída: a base tem que ser exatamente a nota selecionada.
            if(basesDaDevolucao != setOf(notaSaida.docEntry))
                throw Exception("A devolução ${devolucao.docNum} não foi gerada a partir da nota de saída selecionada")
        } else {
            // Sem documento base: valida item + quantidade linha a linha contra a própria nota.
            val qtdNota = notaSaida.DocumentLines
                .filter { it.ItemCode != null }
                .groupBy { it.ItemCode!! }
                .mapValues { (_, ls) -> ls.sumOf { BigDecimal(it.Quantity.replace(',', '.')) } }
            devolucao.DocumentLines
                .filter { it.ItemCode != null }
                .groupBy { it.ItemCode!! }
                .forEach { (item, ls) ->
                    val qDev = ls.sumOf { BigDecimal(it.Quantity.replace(',', '.')) }
                    if(qDev.compareTo(qtdNota[item] ?: BigDecimal.ZERO) > 0)
                        throw Exception("A devolução ${devolucao.docNum} tem item/quantidade que não confere com a " +
                            "nota de saída selecionada (item $item)")
                }
        }

        val totalDev = BigDecimal(devolucao.DocTotal ?: throw Exception("Devolução sem total"))
            .setScale(2, RoundingMode.HALF_UP)
        val totalNota = BigDecimal(notaSaida.DocTotal ?: throw Exception("Nota de saída sem total"))
            .setScale(2, RoundingMode.HALF_UP)
        if(totalDev.compareTo(totalNota) != 0)
            throw Exception("O total da devolução ($totalDev) não bate com a nota de saída informada ($totalNota)")

        // Idempotência mestre: o flag U_vf_estornada só é setado no fim do fluxo (passo 7).
        // Se já está 1, a operação foi concluída antes (ou a nota foi liberada por outro fluxo)
        // — não reprocessa.
        if(notaSaida.U_vf_estornada == 1)
            throw Exception("A nota de saída ${notaSaida.docNum} já foi estornada/vinculada a uma devolução")

        // "Já conciliada" precisa ser o PAR EXATO (esta devolução × esta nota), não apenas o
        // tipo do documento — senão uma devolução conciliada com outra fatura seria tratada
        // como reprocessamento e o passo 5 (conciliação nota × devolução) seria pulado
        // indevidamente.
        val devolucaoJaConciliada = internalReconciliationsService.reconciliacaoEntre(
            devolucao.docEntry ?: -1, DocumentTypes.oCreditNotes.value,
            notaSaida.docEntry ?: -1, DocumentTypes.oInvoices.value).isNotEmpty()

        // A devolução avulsa deve estar aberta ou — em reprocessamento — conciliada exatamente
        // com ESTA nota. Conciliada com qualquer OUTRO documento é a devolução errada: barra
        // antes de qualquer lançamento.
        val devolucaoTemConciliacao = internalReconciliationsService
            .contrapartidasReconciliacao(devolucao.docEntry ?: -1, DocumentTypes.oCreditNotes.value)
            .isNotEmpty()
        if(devolucaoTemConciliacao && !devolucaoJaConciliada)
            throw Exception("A devolução ${devolucao.docNum} já está conciliada com outro documento; " +
                "não pode ser vinculada a esta nota")

        // Estado da nota: só aberta (sem conciliação) ou conciliada com reclassificação (30).
        // Se já estiver conciliada com uma devolução (14) que NÃO seja esta, é provável
        // cancelamento em duplicidade.
        val notaContrapartidas = internalReconciliationsService
            .contrapartidasReconciliacao(notaSaida.docEntry ?: -1, DocumentTypes.oInvoices.value)
        if(notaContrapartidas.contains(DocumentTypes.oCreditNotes.value) && !devolucaoJaConciliada)
            throw Exception("A nota de saída já está conciliada com uma devolução (provável cancelamento em duplicidade)")

        // A nota só deve estar conciliada com a reclassificação (lançamento contábil, 30) e/ou,
        // em reprocessamento, com esta devolução (14). Qualquer outra conciliação ativa
        // (ex.: pagamento) é estado inesperado: barra aqui para não desfazer, mais adiante,
        // vínculos financeiros não relacionados.
        val tiposEsperados = setOf(DocumentTypes.oJournalEntries.value, DocumentTypes.oCreditNotes.value)
        val tiposInesperados = notaContrapartidas.filter { it !in tiposEsperados }
        if(tiposInesperados.isNotEmpty())
            throw Exception("A nota de saída possui conciliações não esperadas (tipos ${tiposInesperados.joinToString()}); " +
                "resolva no SAP antes de vincular a devolução")

        // Reclassificação já apropriada (adiantamento consumido, transcode VFEC): cenário mais
        // complexo, tratado à parte. Barra antes de qualquer lançamento para não sujar a contabilidade.
        if(estornoReclassificacaoService.apropriacoes(listOf(notaSaida.docEntry ?: -1)).isNotEmpty())
            throw Exception("A reclassificação desta nota já teve o adiantamento apropriado; " +
                "faça o acerto dessa devolução manualmente no SAP por enquanto")

        // A reversão (passo 6.1) depende da reclassificação inicial (VFET/VFEC) da nota. Se a
        // nota está aberta e nunca foi reclassificada, não há o que reverter. Localiza aqui,
        // ainda na validação, para falhar ANTES de qualquer lançamento — do contrário o
        // vínculo, a conciliação do cliente e o VFDV já teriam sido postados sem rollback.
        val reclassificacao = journalEntriesService
            .getAll(JournalEntry::class.java, Filter(Predicate("Reference", notaSaida.docNum ?: "", Condicao.EQUAL)))
            .firstOrNull {
                it.TransactionCode == TransactionCodeTypes.VFET.toString() ||
                it.TransactionCode == TransactionCodeTypes.VFEC.toString()
            } ?: throw Exception("A nota de saída ${notaSaida.docNum} não possui reclassificação (VFET/VFEC) a reverter")

        // ---- Execução (cada passo é idempotente: um retry após falha parcial é seguro) ----
        // 3. Vincular a devolução ao contrato (pula se já vinculada).
        if(devolucao.U_venda_futura != contrato.DocEntry)
            creditNotesService.update("{\"U_venda_futura\": ${contrato.DocEntry}}", devolucao.docEntry.toString())

        // 4. Cancelar a conciliação da reclassificação — APENAS a reconciliação que liga esta
        // nota ao lançamento de reclassificação (VFET/VFEC). Não mexe em outras conciliações da
        // nota (ex.: pagamentos), para não desfazer vínculos financeiros não relacionados.
        // Idempotente: a query só traz reconciliações ativas, então uma já cancelada não volta.
        internalReconciliationsService.reconciliacaoEntre(
            notaSaida.docEntry ?: -1, DocumentTypes.oInvoices.value,
            reclassificacao.JdtNum ?: -1, DocumentTypes.oJournalEntries.value)
            .forEach {
                internalReconciliationsService
                    .serviceCancel("{ \"InternalReconciliationParams\": { \"ReconNum\": \"${it.ReconNum}\"} }")
            }

        // Rebusca por DocEntry (GET único traz o documento completo) já no estado atual:
        // nota reaberta pelo passo 4 e devolução vinculada pelo passo 3. A devolução pode não
        // ter parcelas — nesse caso a reconciliação usa o documento inteiro (ver Document.getReconciliationRows).
        val notaSaidaConciliar = invoiceService.getById(notaSaida.docEntry ?: -1).tryGetValue<Invoice>()
        val devolucaoConciliar = creditNotesService.getById(devolucao.docEntry ?: -1).tryGetValue<Invoice>()

        // 5. Conciliar o cliente (nota de saída × devolução) — pula se já conciliada.
        if(!devolucaoJaConciliada)
            internalReconciliationsService.save(InternalReconciliationsBuilder(notaSaidaConciliar, devolucaoConciliar).build())

        // 6. Reverter a reclassificação (VFDV) + estorno do adiantamento. Idempotente:
        // saveOrRecouverReference recupera o VFDV pela Reference em vez de duplicar.
        val vfdv = estornoReclassificacaoService.estornar(devolucaoConciliar, listOf(notaSaida.docEntry ?: -1))

        // 6.1. Conciliar a reclassificação inicial com o estorno (VFDV). Pula se a reclassificação
        // já está conciliada com outro lançamento contábil — que, após o passo 4, só pode ser o VFDV.
        val reclassJaConciliadaComVfdv = internalReconciliationsService
            .contrapartidasReconciliacao(reclassificacao.JdtNum ?: -1, DocumentTypes.oJournalEntries.value)
            .contains(DocumentTypes.oJournalEntries.value)
        if(!reclassJaConciliadaComVfdv)
            internalReconciliationsService.save(InternalReconciliationsBuilder(reclassificacao, vfdv).build())

        // 7. Fechar o flag para o schedule estorno() não reprocessar e ocultar a nota na UI
        // (não pode mais ser estornada/selecionada, pois já recebeu esta devolução). Updates
        // idempotentes (setam o mesmo valor). U_vf_estornada só é setado aqui, no fim.
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
