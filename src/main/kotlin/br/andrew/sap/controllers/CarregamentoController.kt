package br.andrew.sap.controllers.documents

import LogisticaPayload
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.BatchesGroupByItemCode
import br.andrew.sap.model.logistica.Carregamento
import br.andrew.sap.model.logistica.CarregamentoDto
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
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.stock.ItemsService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.Date

@RestController
@RequestMapping("carregamento")
class CarregamentoController(val carregamentoServico: CarregamentoService,
                             val itemService : ItemsService,
                             val invoiceService: InvoiceService,
                             val pedidoVendaService : OrdersService,
                             val batchService : BatchService,
                             val comissaoService: ComissaoService,
                             val telegramService : TelegramRequestService,
                             val applicationEventPublisher: ApplicationEventPublisher,
                             val sqlQueriesService: SqlQueriesService) {

    val logger = LoggerFactory.getLogger(QuotationsController::class.java)


    @GetMapping("")
    fun get(auth : Authentication, page : Pageable): ResponseEntity<Page<Carregamento>> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val pageResult = carregamentoServico.get(Filter(),
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
    fun save(@RequestBody dto: CarregamentoDto){
        val isNewOrder = dto.ordemCarregamento.DocEntry == null
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
            val tripleAdicionar = dto.pedidos.map{
                val order = pedidoVendaService.getById(it).tryGetValue<OrderSales>()

                val pedidosUpdate = PedidoUpdate(
                    order.DocEntry.toString(),
                    order.DocumentLines.map { PedidoUpdateLine(it.DocEntry!!,it.LineNum!!,ordemCriada.DocEntry) }
                )
                Triple(BatchMethod.PATCH,pedidosUpdate,pedidoVendaService)
            }

            val tripleRemover = dto.pedidosRemover.map{
                val order = pedidoVendaService.getById(it).tryGetValue<OrderSales>()

                val pedidosUpdate = PedidoUpdate(
                    order.DocEntry.toString(),
                    order.DocumentLines.map { PedidoUpdateLine(it.DocEntry!!,it.LineNum!!,null) }
                )
                Triple(BatchMethod.PATCH,pedidosUpdate,pedidoVendaService)
            }
            batchService.run(BatchList().addAll(tripleAdicionar).addAll(tripleRemover))
        }catch (e : Exception){
            if(isNewOrder){
                ordemCriada.also { it.U_Status = "Falhou" }
                carregamentoServico.update(ordemCriada,ordemCriada.DocEntry.toString())
            }
            throw e
        }
    }

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id : String, auth : Authentication): ResponseEntity<Carregamento> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val result = carregamentoServico.cancelar(id)

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

    @GetMapping("estoque-em-carregamento")
    fun getEstoqueEmCarregamento(@RequestParam("ItemCode") ItemCode: String): ResponseEntity<Int> {
        val result = carregamentoServico.getEstoqueEmCarregamento("'$ItemCode'")
        return ResponseEntity.ok(result)
    }

    @PostMapping("/generate-from-loading-order/{docEntry}")
    fun saveForAngular(@PathVariable docEntry: Int, @RequestBody lotesAgrupados: List<BatchesGroupByItemCode>): List<BatchResponse> {
        val docEntrys = carregamentoServico.docEntryPedido(docEntry).map { it.DocEntry }
        val pedidos = pedidoVendaService.getAll(OrderSales::class.java, Filter("DocEntry", docEntrys, Condicao.IN))
        val carregamento = carregamentoServico.getById(docEntry.toString()).tryGetValue<Carregamento>()

        pedidos.forEach { order ->
            order.DocumentLines
                .filter { it.U_ORD_CARREGAMENTO == docEntry }
                .forEach { currentItem ->
                    val lotes = lotesAgrupados
                        .firstOrNull { it.ItemCode == currentItem.ItemCode }
                        ?.Batches
                        ?: throw Exception("Erro ao fazer bind de lote para item ${currentItem.ItemCode}")

                    lotes.forEach { batch ->
                        batch.ItemCode = currentItem.ItemCode
                    }
                    currentItem.BatchNumbers = lotes
                }
        }

        val batchList = BatchList()

        pedidos.forEach { pedido ->
            val bplId = pedido.getBPL_IDAssignedToInvoice()
            val sequenceCodes = carregamentoServico.procuraSequenceCode(bplId)
            val sequenceCode = sequenceCodes.firstOrNull()
                ?: throw Exception("Nenhum SequenceCode encontrado para a filial $bplId")
            val seqCodeValue = sequenceCode.SeqCode
                ?: throw Exception("SeqCode não encontrado para a filial $bplId")
            val documento = pedido.toDocument(DocumentTypes.oInvoices, seqCodeValue)

            documento.getOrCreateTaxExtension().Vehicle = carregamento.U_placa
            documento.getOrCreateTaxExtension().Carrier = carregamento.U_transportadora
            documento.ClosingRemarks = "Motorista: ${carregamento.U_motorista}"

            documento.U_faturadoOrdemCarregamento = docEntry
            documento.docDate = null
            batchList.add(BatchMethod.POST, documento, invoiceService)
        }
        return batchService.run(batchList)
    }

    @PostMapping("/{id}/status")
    fun atualizarStatus(@PathVariable id: String, @RequestBody payload: Map<String, String>, auth: Authentication): ResponseEntity<Any> {
        if (auth !is User) {
            return ResponseEntity.noContent().build()
        }

        try {
            carregamentoServico.update(payload, id)
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
                "U_transportadora" to payload.U_transportadora
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
        val filter = Filter(Predicate("U_faturadoOrdemCarregamento", idCarregamento, Condicao.EQUAL))
        return listOf(invoiceService)
            .map { it.getAll(Document::class.java,filter) }
            .flatMap { it }
            .sortedWith(compareBy(
                { it.docDate },
                { it.docObjectCode?.ordinal }
            ))
    }

    @GetMapping("/search")
    fun search(@RequestParam("dataInicial", required = false) dataInicial: String?,
               @RequestParam("dataFinal", required = false) dataFinal: String?,
               @RequestParam("filial") filial: Int,
               @RequestParam("localidade") localidade: String): NextLink<OrderSales> {
        val startDate = dataInicial ?: "1900-01-01"
        val endDate = dataFinal ?: "2100-12-31"
        val result = pedidoVendaService.fullSearchTextFallBack(startDate, endDate, filial, localidade)
        return result ?: NextLink(emptyList(), "")
    }
}

// Todo 2 pedidos que tenha 3 itens ao total 1 dos itens está em ambos os pedidos.
// Todo Faturar 2 pedidos com produtos distintos, colocar um breakpoint no inicio do projeto, desativa o item do 2 pedido enquanto ta no breakpoint.
// Todo Objetivo é dar roolback em todos os pedidos tem que todos passar não apenas um.
