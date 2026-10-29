package br.andrew.sap.controllers.logistica
import br.andrew.sap.model.logistica.LogisticaPayload
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sap.BatchesGroupByItemCode
import br.andrew.sap.model.logistica.Carregamento
import br.andrew.sap.model.logistica.CarregamentoDto
import br.andrew.sap.model.logistica.RetiradaFatura
import br.andrew.sap.model.logistica.PedidoUpdate
import br.andrew.sap.model.logistica.PedidoUpdateLine
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.*
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchResponse
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.documents.DeliveryService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.stock.ItemsService
import org.springframework.beans.factory.annotation.Value
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.Date
import br.andrew.sap.services.logistica.CarregamentoService
import br.andrew.sap.services.integracao.TelegramRequestService
import br.andrew.sap.model.sap.comercial.DocEntry
import br.andrew.sap.model.sap.documents.base.DocumentLines

@RestController
@RequestMapping("carregamento")
class CarregamentoController(val carregamentoServico: CarregamentoService,
                             val itemService : ItemsService,
                             val invoiceService: InvoiceService,
                             val deliveryService: DeliveryService,
                             val pedidoVendaService : OrdersService,
                             val batchService : BatchService,
                             val comissaoService: ComissaoService,
                             val telegramService : TelegramRequestService,
                             val applicationEventPublisher: ApplicationEventPublisher,
                             val sqlQueriesService: SqlQueriesService,
                             @Value("\${carregamento.copiar-anexo:true}") val copiarAnexo: Boolean) {

    val logger = LoggerFactory.getLogger(CarregamentoController::class.java)

    /*
     * carregamento.copiar-anexo=false: nota e entrega saem sem anexo. O Service Layer copia sozinho o
     * anexo do documento base (pedido/NF de venda futura) e recusa com 131-102 quando nao tem o mount
     * Linux da pasta de anexos (HOMOLOG2). Null ou campo ausente nao evitam a copia; -1 evita
     * (conferido no HOMOLOG2 em 2026-10-27).
     */
    private val SEM_ANEXO = -1
    val objectMapper = ObjectMapper().registerModule(KotlinModule())



    @GetMapping("")
    fun get(auth : Authentication,
            page : Pageable,
            @RequestParam(required = false) id: Int?,
            @RequestParam(required = false) status: String?,
            @RequestParam(required = false) createDateFrom: String?,
            @RequestParam(required = false) createDateTo: String?): ResponseEntity<Page<Carregamento>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val filter = Filter().also {
            if (id != null) {
                it.add(Predicate("DocEntry", id, Condicao.EQUAL))
            }
            if (!status.isNullOrBlank()) {
                it.add(Predicate("U_Status", status.trim(), Condicao.EQUAL))
            }
            if (!createDateFrom.isNullOrBlank()) {
                it.add(Predicate("CreateDate", createDateFrom.trim(), Condicao.GREAT_EQUAL))
            }
            if (!createDateTo.isNullOrBlank()) {
                it.add(Predicate("CreateDate", createDateTo.trim(), Condicao.LESS_EQUAL))
            }
        }

        val pageResult = carregamentoServico.get(filter,
            OrderBy(mapOf("CreateDate" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<Carregamento>(page)

        val pageEnriched = carregamentoServico.preencherTotais(pageResult)

        return ResponseEntity.ok(pageEnriched)
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

    @PostMapping()
    fun save(@RequestBody dto: CarregamentoDto, auth: Authentication?): Carregamento {
        val usuario = nomeUsuario(auth)
        //Edicao usa a mesma trava da geracao: um pedido vinculado enquanto as notas sao geradas
        //ficaria sem nota.
        val id = dto.ordemCarregamento.DocEntry ?: return salvar(dto, usuario)
        if (!gerandoNotas.add(id))
            throw Exception("A ordem $id esta gerando notas ou sendo salva; aguarde terminar.")
        try {
            return salvar(dto, usuario)
        } finally {
            gerandoNotas.remove(id)
        }
    }

    private fun salvar(dto: CarregamentoDto, usuario: String): Carregamento {
        //Retiradas de fatura so passam por salvarRetiradas, que valida saldo e substitui a colecao:
        //um PATCH comum faria MERGE e duplicaria linha.
        dto.ordemCarregamento.ORD_CRG_LINHACollection = null
        val isNewOrder = dto.ordemCarregamento.DocEntry == null
        //Ordem com nota emitida nao muda: um pedido incluido agora seria fechado sem nota pela
        //recuperacao de saveForAngular, e retirada nova nao teria entrega.
        if (!isNewOrder && getNotasByCarregamentos(dto.ordemCarregamento.DocEntry!!).isNotEmpty())
            throw Exception("A ordem ${dto.ordemCarregamento.DocEntry} ja tem notas geradas e nao pode ser alterada.")
        //Edicao: se o vinculo dos pedidos falhar, as retiradas voltam a ser as de antes.
        val retiradasAntes = if (isNewOrder || dto.faturasReserva == null) null
            else carregamentoServico.getById(dto.ordemCarregamento.DocEntry.toString()).tryGetValue<Carregamento>()
                .retiradasDeFatura().map { RetiradaFatura(it.U_docEntryFatura!!, it.U_baseLine!!, it.U_qtdRetirada!!) }
        dto.ordemCarregamento.U_usuario = usuario
        //So vira "Criada"/"Alterada" no fim, depois de vincular os pedidos: se algo cair no meio, o historico nao
        //diz que deu certo.
        dto.ordemCarregamento.U_acao = (if (isNewOrder) "Criação" else "Alteração") + CarregamentoService.EM_ANDAMENTO
        val ordemCriada = if(isNewOrder){
            carregamentoServico.save(dto.ordemCarregamento).tryGetValue<Carregamento>()
        }else{
            val id = dto.ordemCarregamento.DocEntry.toString()

            dto.ordemCarregamento.docEntryQuantity = null
            dto.ordemCarregamento.Weight1 = null

            carregamentoServico.update(dto.ordemCarregamento, id)
            dto.ordemCarregamento
            //TODO arrumar esse updatge provavelmente se fizer o bind no front vai funcionar
            //carregamentoServico.update(dto.ordemCarregamento,dto.ordemCarregamento.DocEntry.toString())!!.tryGetValue<Carregamento>()
        }

        try {
            val vinculadosAtualmente = if (!isNewOrder)
                carregamentoServico.docEntryPedido(ordemCriada.DocEntry!!).map { it.DocEntry }
            else
                emptyList()

            val pedidosParaAdicionar = dto.pedidos.filter { it !in vinculadosAtualmente }
            val pedidosParaRemover = vinculadosAtualmente.filter { it !in dto.pedidos }

            val tripleAdicionar = pedidosParaAdicionar.map {
                val order = pedidoVendaService.getById(it.toString()).tryGetValue<OrderSales>()
                val pedidosUpdate = PedidoUpdate(
                    order.DocEntry.toString(),
                    order.DocumentLines.map { line -> PedidoUpdateLine(line.DocEntry!!,line.LineNum!!,ordemCriada.DocEntry?.toString()) },
                    order.docNum
                )
                Triple(BatchMethod.PATCH,pedidosUpdate,pedidoVendaService)
            }

            val tripleRemover = pedidosParaRemover.map {
                val order = pedidoVendaService.getById(it.toString()).tryGetValue<OrderSales>()
                val pedidosUpdate = PedidoUpdate(
                    order.DocEntry.toString(),
                    order.DocumentLines.map { line -> PedidoUpdateLine(line.DocEntry!!,line.LineNum!!,null) },
                    order.docNum
                )
                Triple(BatchMethod.PATCH,pedidosUpdate,pedidoVendaService)
            }

            //Retiradas antes de vincular pedido: se o saldo da NF nao bastar, nenhum pedido fica preso
            //numa ordem que falhou (a busca so traz pedido sem U_ORD_CARREGAMENTO).
            if (dto.faturasReserva != null && (!isNewOrder || dto.faturasReserva.isNotEmpty()))
                carregamentoServico.salvarRetiradas(ordemCriada.DocEntry!!, dto.faturasReserva)

            if (tripleAdicionar.isNotEmpty() || tripleRemover.isNotEmpty())
                batchService.run(BatchList().addAll(tripleAdicionar).addAll(tripleRemover))
            registrarAcao(ordemCriada.DocEntry!!, usuario, if (isNewOrder) "Criada" else "Alterada")
        }catch (e : Exception){
            if(isNewOrder){
                ordemCriada.also { it.U_Status = "Falhou"; it.U_usuario = usuario; it.U_acao = acaoComErro("Falhou", e) }
                carregamentoServico.update(ordemCriada,ordemCriada.DocEntry.toString())
            }
            if (retiradasAntes != null) runCatching { carregamentoServico.salvarRetiradas(ordemCriada.DocEntry!!, retiradasAntes) }
                .onFailure { logger.error("Carregamento ${ordemCriada.DocEntry} - retiradas nao voltaram ao estado anterior", it) }
            //Depois de devolver as retiradas: a versao "recusada" do historico ja mostra o estado final.
            if (!isNewOrder) registrarAcao(ordemCriada.DocEntry!!, usuario, acaoComErro("Alteração recusada", e))
            throw e
        }
        return ordemCriada
    }

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id : String, auth : Authentication): ResponseEntity<Carregamento> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val result = carregamentoServico.cancelar(id, nomeUsuario(auth))

        return ResponseEntity.ok(result)
    }

    @PostMapping("/save-carregamento")
    fun saveCarregamento(@RequestBody ordem: Carregamento): ResponseEntity<Any> {
        return try {
            if(ordem.U_Status.isNullOrEmpty()) {
                ordem.U_Status = "Aberto"
            }

            val ordemCriada = carregamentoServico.save(ordem).tryGetValue<Carregamento>()
            ResponseEntity.ok(ordemCriada)
        } catch (e: Exception) {
            logger.error("Erro ao salvar ordem de carregamento", e)
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("estoque-em-carregamento-itens")
    fun getEstoqueEmCarregamentoItens(): Map<String, Double> = carregamentoServico.estoqueEmCarregamentoPorItem()

    @PostMapping("/generate-from-loading-order/{docEntry}")
    fun saveForAngular(@PathVariable docEntry: Int, @RequestBody lotesAgrupados: List<BatchesGroupByItemCode>,
                       auth: Authentication?): List<BatchResponse> {
        //Duplo clique ou duas abas: a segunda chamada leria "sem notas" antes de a primeira gravar.
        //shortcut: trava so nesta instancia; com mais de uma instancia atendendo a mesma tela, trocar
        //por uma transicao de status atomica no SAP.
        if (!gerandoNotas.add(docEntry))
            throw Exception("A ordem $docEntry esta gerando notas ou sendo salva; aguarde terminar.")
        try {
            return gerarNotas(docEntry, lotesAgrupados, nomeUsuario(auth))
        } finally {
            gerandoNotas.remove(docEntry)
        }
    }

    private fun gerarNotas(docEntry: Int, lotesAgrupados: List<BatchesGroupByItemCode>, usuario: String): List<BatchResponse> {
        //Nunca gerar de novo: uma NF de venda futura com saldo aceitaria uma segunda entrega. Se ja
        //existe nota/entrega da ordem (ex.: tentativa anterior que gravou e nao fechou), so fecha a
        //ordem e os pedidos dela.
        val jaGeradas = getNotasByCarregamentos(docEntry)
        if (jaGeradas.isNotEmpty()) {
            fecharOrdem(docEntry, usuario, "Fechada (as notas já existiam)")
            fecharPedidos(docEntry, carregamentoServico.docEntryPedido(docEntry).map { it.DocEntry.toString() })
            throw Exception("A ordem $docEntry ja tem notas geradas (${jaGeradas.mapNotNull { it.docNum }.distinct().joinToString()}). " +
                "Ela foi marcada como Fechada; confira na aba Notas Emitidas.")
        }

        val docEntrys = carregamentoServico.docEntryPedido(docEntry).map { it.DocEntry }
        val pedidos = if (docEntrys.isEmpty()) emptyList()
            else pedidoVendaService.getAll(OrderSales::class.java, Filter("DocEntry", docEntrys, Condicao.IN))
        val carregamento = carregamentoServico.getById(docEntry.toString()).tryGetValue<Carregamento>()

        val porPedido = lotesAgrupados.any { it.DocEntry != null }
        //Mesmo DocEntry pode existir na ORDR e na OINV: o lote do pedido nunca casa com o da fatura.
        val lotesDePedido = lotesAgrupados.filter { it.BaseType != DocumentTypes.oInvoices.value }

        pedidos.forEach { order ->
            order.DocumentLines
                .filter { it.U_ORD_CARREGAMENTO == docEntry }
                .forEach { currentItem ->
                    val lotesDoItem = if (porPedido)
                        lotesDePedido.find { it.DocEntry == order.DocEntry?.toIntOrNull() && it.ItemCode == currentItem.ItemCode }
                    else
                        lotesAgrupados.find { it.ItemCode == currentItem.ItemCode }

                    if (lotesDoItem != null) {
                        currentItem.BatchNumbers = if (porPedido)
                            lotesDoItem.Batches ?: emptyList()
                        else
                            lotesDoItem.getBachesBy(currentItem)
                        logSelecaoIncompletaDeLote(docEntry, order, currentItem)
                    } else {
                        logger.warn("Carregamento $docEntry - Pedido ${order.docNum ?: order.docEntry} sem lote: ${detalharLinhaLote(currentItem)}")
                    }
                }
        }

        val lotesParaFaturamento = mutableListOf<Pair<OrderSales, Document>>()
        val invoiceBatchList = BatchList()
        val pedidosParaFechar = mutableListOf<String>()

        pedidos.forEach { pedido ->
            val bplId = pedido.getBPL_IDAssignedToInvoice()
            val sequenceCodes = carregamentoServico.procuraSequenceCode(bplId)
            val seqCodeValue = sequenceCodes.firstOrNull()?.SeqCode
                ?: throw Exception("SeqCode não encontrado para a filial $bplId")

            val linhasDoCarregamento = pedido.DocumentLines.filter { it.U_ORD_CARREGAMENTO == docEntry }
            logger.info("Carregamento $docEntry - Pedido ${pedido.docNum ?: pedido.docEntry}: ${linhasDoCarregamento.size} linhas -> " +
                linhasDoCarregamento.joinToString(" | ") { detalharLinhaLote(it) })
            val pedidoParaFaturar = OrderSales(pedido.CardCode, pedido.DocDueDate, linhasDoCarregamento, bplId).also { p ->
                p.docObjectCode = pedido.docObjectCode
                p.salesPersonCode = pedido.salesPersonCode
                p.paymentGroupCode = pedido.paymentGroupCode
                p.model = pedido.model
                p.journalMemo = pedido.journalMemo
                p.docDate = pedido.docDate
                p.controlAccount = pedido.controlAccount
                p.documentAdditionalExpenses = pedido.documentAdditionalExpenses.toMutableList()
            }

            val documento = pedidoParaFaturar.toDocument(DocumentTypes.oInvoices, seqCodeValue)

            documento.AttachmentEntry = if (copiarAnexo) pedido.AttachmentEntry else SEM_ANEXO
            documento.getOrCreateTaxExtension().Vehicle = carregamento.U_placa
            documento.getOrCreateTaxExtension().Carrier = carregamento.U_transportadora
            documento.ClosingRemarks = "Motorista: ${carregamento.U_motorista}. Ordem: ${carregamento.DocEntry}"
            documento.U_faturadoOrdemCarregamento = docEntry
            //Com Comments preenchido o SAP nao acrescenta o "Based On ..." que poria sozinho.
            documento.comments = CarregamentoService.observacaoComOrdem(docEntry, pedido.comments,
                pedido.docNum?.let { "Based On Sales Orders $it." })

            documento.docDate = null
            documento.DocDueDate = null

            lotesParaFaturamento.add(pedido to documento)
            invoiceBatchList.add(BatchMethod.POST, documento, invoiceService)
            val orderId = pedido.DocEntry ?: pedido.docEntry?.toString()
                ?: throw IllegalStateException("DocEntry do pedido nao encontrado para fechamento")
            pedidosParaFechar.add(orderId)
        }

        //Entregas de fatura de reserva vao no mesmo changeset das notas: falhou uma, nada fica.
        val entregas = carregamentoServico.entregasDaOrdem(carregamento) { fatura, linha ->
            val lotesDaLinha = if (porPedido)
                lotesAgrupados.find { it.BaseType == DocumentTypes.oInvoices.value && it.DocEntry == fatura &&
                    it.ItemCode == linha.ItemCode && it.BaseLine == linha.BaseLine }
            else
                lotesAgrupados.find { it.ItemCode == linha.ItemCode }
            when {
                lotesDaLinha == null -> listOf<br.andrew.sap.model.producao.BatchStock>().also {
                    logger.warn("Carregamento $docEntry - Fatura $fatura sem lote: ${detalharLinhaLote(linha)}")
                }
                porPedido -> lotesDaLinha.Batches ?: emptyList()
                else -> lotesDaLinha.getBachesBy(linha)
            }
        }
        entregas.forEach {
            it.U_faturadoOrdemCarregamento = docEntry
            if (!copiarAnexo) it.AttachmentEntry = SEM_ANEXO
            it.getOrCreateTaxExtension().Vehicle = carregamento.U_placa
            it.getOrCreateTaxExtension().Carrier = carregamento.U_transportadora
            it.ClosingRemarks = "Motorista: ${carregamento.U_motorista}. Ordem: ${carregamento.DocEntry}"
            invoiceBatchList.add(BatchMethod.POST, it, deliveryService)
        }

        val resposta = try {
            batchService.run(invoiceBatchList)
        } catch (e: Exception) {
            logger.error("Erro ao finalizar notas do carregamento $docEntry: ${e.message}", e)
            registrarAcao(docEntry, usuario, acaoComErro("Geração de notas recusada", e))
            lotesParaFaturamento.forEach { (pedido, documento) ->
                val linhasDoCarregamento = pedido.DocumentLines.filter { it.U_ORD_CARREGAMENTO == docEntry }
                val payload = objectMapper.writeValueAsString(documento)
                logger.error(
                    "Carregamento $docEntry - Pedido ${pedido.docNum ?: pedido.docEntry} payload enviado ao SAP: $payload"
                )
                logger.error("Carregamento $docEntry - Pedido ${pedido.docNum ?: pedido.docEntry} linhas para faturamento: " +
                    linhasDoCarregamento.joinToString(" | ") { detalharLinhaLote(it) })
            }
            throw e
        }

        //Notas e entregas gravadas: a ordem fecha aqui, sem depender da tela nem do fechamento dos
        //pedidos. Fechar pedido e arrumacao: um por vez, porque o SAP as vezes ja fechou o pedido todo
        //faturado e recusa fechar de novo - num changeset unico essa recusa desfazia o fechamento dos
        //outros. Falha so registra: devolver erro faria a tela tentar faturar outra vez.
        fecharOrdem(docEntry, usuario, "Notas geradas: ${pedidos.size} nota(s) e ${entregas.size} entrega(s)")
        fecharPedidos(docEntry, pedidosParaFechar)
        return resposta
    }

    private fun fecharPedidos(docEntry: Int, ids: List<String>) {
        ids.forEach { id ->
            try {
                pedidoVendaService.close(id)
            } catch (e: Exception) {
                logger.warn("Carregamento $docEntry - nota gerada, mas o pedido DocEntry $id nao fechou: ${e.message}")
            }
        }
    }

    private val gerandoNotas = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()

    //Chamado com as notas ja gravadas: fechar nunca pode falhar por causa do historico. Se a gravacao com
    //usuario/acao for recusada (ex.: Service Layer com metadado velho depois de criar os campos, "Property
    //'U_acao' is invalid"), fecha so o status - a tela daria erro mesmo com as notas criadas.
    private fun fecharOrdem(docEntry: Int, usuario: String, acao: String) {
        try {
            carregamentoServico.update(mapOf("U_Status" to "Fechado", "U_usuario" to usuario, "U_acao" to acao), docEntry.toString())
        } catch (e: Exception) {
            logger.warn("Carregamento $docEntry - fechamento sem historico (usuario/acao): ${e.message}")
            carregamentoServico.update(mapOf("U_Status" to "Fechado"), docEntry.toString())
        }
    }

    //Tentativa que nao gravou nada tambem vai para o historico; falhar aqui nao esconde o erro original.
    private fun registrarAcao(docEntry: Int, usuario: String, acao: String) {
        runCatching { carregamentoServico.update(mapOf("U_usuario" to usuario, "U_acao" to acao), docEntry.toString()) }
            .onFailure { logger.warn("Carregamento $docEntry - historico nao gravado: ${it.message}") }
    }

    private fun acaoComErro(acao: String, e: Exception) = "$acao: ${e.message}".take(254)

    //Login de quem usa a tela; o UserSign do SAP e sempre o usuario do Service Layer.
    private fun nomeUsuario(auth: Authentication?): String =
        (auth as? User)?.userName?.takeIf { it.isNotBlank() } ?: auth?.name?.takeIf { it.isNotBlank() } ?: "desconhecido"


    private fun logSelecaoIncompletaDeLote(docEntry: Int, pedido: OrderSales, line: br.andrew.sap.model.sap.documents.base.DocumentLines) {
        val quantidadeLinha = BigDecimal(line.Quantity)
        val quantidadeLotes = line.BatchNumbers.fold(BigDecimal.ZERO) { acc, batch -> acc + BigDecimal(batch.Quantity) }
        if (quantidadeLinha.compareTo(quantidadeLotes) != 0) {
            logger.warn("Carregamento $docEntry - Pedido ${pedido.docNum ?: pedido.docEntry} com seleção incompleta de lote: ${detalharLinhaLote(line)}")
        }
    }

    private fun detalharLinhaLote(line: br.andrew.sap.model.sap.documents.base.DocumentLines): String {
        val quantidadeLotes = line.BatchNumbers.fold(BigDecimal.ZERO) { acc, batch -> acc + BigDecimal(batch.Quantity) }
        val lotes = if (line.BatchNumbers.isEmpty()) {
            "sem lotes"
        } else {
            line.BatchNumbers.joinToString(",") { "${it.BatchNumber}:${it.Quantity}" }
        }
        return "Item=${line.ItemCode} Qty=${line.Quantity} QtyLotes=$quantidadeLotes Whs=${line.WarehouseCode} LineDocEntry=${line.DocEntry} LineNum=${line.LineNum} Lotes=[$lotes]"
    }

    @PostMapping("/{id}/status")
    fun atualizarStatus(@PathVariable id: String, @RequestBody payload: Map<String, String>, auth: Authentication): ResponseEntity<Any> {
        if (auth !is User) {
            return ResponseEntity.noContent().build()
        }

        try {
            carregamentoServico.update(payload + mapOf("U_usuario" to nomeUsuario(auth),
                "U_acao" to "Status alterado para ${payload["U_Status"] ?: "?"}"), id)
            return ResponseEntity.ok(mapOf("message" to "Status atualizado com sucesso."))
        } catch (e: Exception) {
            logger.error("Erro ao atualizar status para o carregamento $id", e)
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }


    @PostMapping("/{id}/cancelar-pedidos")
    fun cancelarPedidos(@PathVariable id: String, @RequestBody request: Map<String, List<Int>>, auth: Authentication): ResponseEntity<Any> {
        if (auth !is User) {
            return ResponseEntity.noContent().build()
        }

        val docNums = request["pedidos"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "Lista de DocNum não fornecida"))

        return try {
            carregamentoServico.cancelarPedidos(id.toInt(), docNums)
            ResponseEntity.ok(mapOf("message" to "Pedidos cancelados com sucesso"))
        } catch (e: Exception) {
            logger.error("Erro ao cancelar pedidos", e)
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/{id}/detalhes")
    fun getDetalhes(@PathVariable id: Int): List<Carregamento> {
        return carregamentoServico.getTotaisPedidoseQuantidade(id)
    }

    @PostMapping("/{id}/logistica")
    fun updateLogistica(@PathVariable id: String, @RequestBody payload: LogisticaPayload, auth: Authentication): ResponseEntity<Any> {
        if (auth !is User) {
            return ResponseEntity.noContent().build()
        }

        try {
            val dadosParaAtualizar = mapOf(
                "U_placa" to payload.U_placa,
                "U_motorista" to payload.U_motorista,
                "U_capacidadeCaminhao" to payload.U_capacidadeCaminhao,
                "U_transportadora" to payload.U_transportadora,
                "U_usuario" to nomeUsuario(auth),
                "U_acao" to "Logística alterada"
            )

            carregamentoServico.update(dadosParaAtualizar, id)
            return ResponseEntity.ok(mapOf("message" to "Dados de logística atualizados com sucesso."))
        } catch (e: Exception) {
            logger.error("Erro ao atualizar dados de logística para o carregamento $id", e)
            return ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/notas/{idCarregamento}")
    fun getNotasByCarregamentos(@PathVariable idCarregamento: Int): List<Document> {
        val filter = Filter(mutableListOf(
            Predicate("U_faturadoOrdemCarregamento", idCarregamento, Condicao.EQUAL),
            Predicate(Cancelled.tNO, Condicao.EQUAL),
        ))
        return listOf(invoiceService, deliveryService)
            .map { it.getAll(Document::class.java,filter) }
            .flatMap { it }
            .sortedWith(compareBy(
                { it.docDate },
                { it.docObjectCode?.ordinal }
            ))
    }

    /** Pedidos da busca do carregamento, paginados por pedido (sap-odbc; ver CarregamentoService.searchPedidos). */
    @GetMapping("/search-pedidos")
    fun searchPedidos(@RequestParam("dataInicial", required = false) dataInicial: String?,
                      @RequestParam("dataFinal", required = false) dataFinal: String?,
                      @RequestParam("filial") filial: Int,
                      @RequestParam("localidade", required = false) localidadeFiltro: String?,
                      @RequestParam("vendedor", required = false) vendedor: Int?,
                      @RequestParam("cidade", required = false) cidade: String?,
                      @RequestParam("pagina", defaultValue = "0") pagina: Int): PaginaDocumentos {
        val (pedidos, temMais) = carregamentoServico.searchPedidos(dataInicial ?: "1900-01-01", dataFinal ?: "2100-12-31",
            filial, localidadeFiltro?.takeIf { it.isNotBlank() } ?: "%", vendedor, cidade, pagina, DOCUMENTOS_POR_PAGINA)
        return PaginaDocumentos(pedidos, temMais)
    }

    /**
     * NF de venda futura com saldo, paginada por documento (os pedidos vem de /search-pedidos).
     * shortcut: o SAP devolve todas as linhas a cada pagina; a pagina so alivia a tela.
     */
    @GetMapping("/search-faturas")
    fun searchFaturas(@RequestParam("dataInicial", required = false) dataInicial: String?,
                      @RequestParam("dataFinal", required = false) dataFinal: String?,
                      @RequestParam("filial") filial: Int,
                      @RequestParam("localidade", required = false) localidadeFiltro: String?,
                      @RequestParam("vendedor", required = false) vendedor: Int?,
                      @RequestParam("cidade", required = false) cidade: String?,
                      @RequestParam("pagina", defaultValue = "0") pagina: Int): PaginaDocumentos {
        val linhas = carregamentoServico.searchFaturasReserva(dataInicial ?: "1900-01-01", dataFinal ?: "2100-12-31",
            filial, localidadeFiltro?.takeIf { it.isNotBlank() } ?: "%", vendedor)
            .filter { CarregamentoService.cidadeConfere(it.CityS, cidade) }
        val documentos = linhas.mapNotNull { it.DocEntry?.toIntOrNull() }.distinct().sorted()
        val daPagina = documentos.drop(pagina * DOCUMENTOS_POR_PAGINA).take(DOCUMENTOS_POR_PAGINA).toSet()
        return PaginaDocumentos(linhas.filter { it.DocEntry?.toIntOrNull() in daPagina },
            documentos.size > (pagina + 1) * DOCUMENTOS_POR_PAGINA)
    }

    data class PaginaDocumentos(val content: List<OrderSales>, val temMais: Boolean)

    @GetMapping("/{id}/historico")
    fun historico(@PathVariable id: Int): List<Map<String, Any?>> = carregamentoServico.historico(id)

    @GetMapping("/notas-status/{idCarregamento}")
    fun statusNotas(@PathVariable idCarregamento: Int): List<Map<String, Any?>> =
        carregamentoServico.statusNotas(idCarregamento)

    @GetMapping("/danfes/{idCarregamento}")
    fun danfes(@PathVariable idCarregamento: Int): ResponseEntity<ByteArray> =
        ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/zip"))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("danfes-ordem-$idCarregamento.zip").build().toString())
            .body(carregamentoServico.danfes(idCarregamento))

    companion object {
        const val DOCUMENTOS_POR_PAGINA = 30
    }
}

// Todo 2 pedidos que tenha 3 itens ao total 1 dos itens está em ambos os pedidos.
// Todo Faturar 2 pedidos com produtos distintos, colocar um breakpoint no inicio do projeto, desativa o item do 2 pedido enquanto ta no breakpoint.
// Todo Objetivo é dar roolback em todos os pedidos tem que todos passar não apenas um.
