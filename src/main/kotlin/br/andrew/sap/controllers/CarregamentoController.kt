package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.producao.BatchStock
import br.andrew.sap.model.sap.BatchesGroupByItemCode
import br.andrew.sap.model.sap.Carregamento
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.services.*
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

@RestController
@RequestMapping("carregamento")
class CarregamentoController(val carregamentoServico: CarregamentoService,
                             val itemService : ItemsService,
                             val invoiceService: InvoiceService,
                             val pedidoVendaService : OrdersService,
                             val batchService : BatchService,
                             val comissaoService: ComissaoService,
                             val telegramService : TelegramRequestService,
                             val applicationEventPublisher: ApplicationEventPublisher) {

    val logger = LoggerFactory.getLogger(QuotationsController::class.java)


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

    @PostMapping("/angular")
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

    @PostMapping("/{id}/cancel")
    fun cancel(@PathVariable id : String, auth : Authentication): ResponseEntity<Carregamento> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val carregamento = carregamentoServico.getById(id).tryGetValue<Carregamento>()
        carregamento.U_Status = "Cancelado"

        val result = carregamentoServico.update(carregamento, id)
        return ResponseEntity.ok(result?.tryGetValue<Carregamento>())
    }

    @PostMapping("/{id}/finalizar")
    fun finalizar(@PathVariable id : String, auth : Authentication): ResponseEntity<Carregamento> {
        if(auth !is User)
            return ResponseEntity.noContent().build()

        val carregamento = carregamentoServico.getById(id).tryGetValue<Carregamento>()
        carregamento.U_Status = "Fechado"

        val result = carregamentoServico.update(carregamento, id)
        return ResponseEntity.ok(result?.tryGetValue<Carregamento>())
    }

    @GetMapping("estoque-em-carregamento")
    fun getEstoqueEmCarregamento(@RequestParam("ItemCode") ItemCode: String): ResponseEntity<Int> {
        val result = carregamentoServico.getEstoqueEmCarregamento("'$ItemCode'")
        return ResponseEntity.ok(result)
    }


//    @PostMapping("/generate-from-loading-order/{docEntry}")
//    fun saveForAngular(@PathVariable docEntry: Int, @RequestBody lotesAgrupados: List<BatchesGroupByItemCode>): List<BatchResponse> {
//        val docEntrys = carregamentoServico.docEntryPedido(docEntry).map { it.DocEntry }
//        val pedidos = pedidoVendaService.getAll(OrderSales::class.java, Filter("DocEntry", docEntrys, Condicao.IN))
//
////         Assign the batch numbers directly (assuming BatchNumbers expects List<BatchStock>)
//        pedidos.forEach { order ->
//            order.DocumentLines.filter { it.U_ORD_CARREGAMENTO2 == docEntry }.forEach { currentItem ->
//                currentItem.BatchNumbers = lotesAgrupados
//                    .filter { it.ItemCode == currentItem.ItemCode }
//                    .first().Batches ?: throw Exception("Erro ao fazer bind de lote")
//            }
//        }
//
//        //TODO Desaggrupar batch
//        //TODO criar no clickup - criar um Bean que recebe a filial e retorna a sequenceCode de NFe (defaultWharehouse)
//        val batchList = BatchList()
//        pedidos.map { it.toDocument(DocumentTypes.oInvoices, 27) }.forEach {
//
////            invoiceService.save(it.also {
////
////                it.DocumentLines.forEach { item -> item.BatchNumbers.forEach {
////                    batch ->
////                    batch.ItemCode = item.ItemCode
////                    batch.inDate = null
////                    batch.itemName = null
////                } }
////            })
//            batchList.add(BatchMethod.POST, it, invoiceService)
//        }
//        return batchService.run(batchList)
//    }
//
////    invoiceService.save(it.also {
////        it.docDate = null
////        it.docNum = null
////        it.sequenceModel = "39"
////        it.SequenceCode = 27
////    })
//
//}


    @PostMapping("/generate-from-loading-order/{docEntry}")
    fun saveForAngular(@PathVariable docEntry: Int, @RequestBody lotesAgrupados: List<BatchesGroupByItemCode>): List<BatchResponse> {
        val docEntrys = carregamentoServico.docEntryPedido(docEntry).map { it.DocEntry }
        val pedidos = pedidoVendaService.getAll(OrderSales::class.java, Filter("DocEntry", docEntrys, Condicao.IN))

//         Assign the batch numbers directly (assuming BatchNumbers expects List<BatchStock>)
        pedidos.forEach { order ->
            order.DocumentLines
                .filter { it.U_ORD_CARREGAMENTO2 == docEntry }
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

        //TODO Desaggrupar batch
        //TODO criar no clickup - criar um Bean que recebe a filial e retorna a sequenceCode de NFe (defaultWharehouse)
        val batchList = BatchList()
        pedidos.map { it.toDocument(DocumentTypes.oInvoices, 27) }.forEach {
            batchList.add(BatchMethod.POST, it, invoiceService)
        }
        return batchService.run(batchList)
    }

//    invoiceService.save(it.also {
//        it.docDate = null
//        it.docNum = null
//        it.sequenceModel = "39"
//        it.SequenceCode = 27
//    })

}

// Todo 2 pedidos que tenha 3 itens ao total 1 dos itens está em ambos os pedidos.
// Todo Faturar 2 pedidos com produtos distintos, colocar um breakpoint no inicio do projeto, desativa o item do 2 pedido enquanto ta no breakpoint.
// Todo Objetivo é dar roolback em todos os pedidos tem que todos passar não apenas um.