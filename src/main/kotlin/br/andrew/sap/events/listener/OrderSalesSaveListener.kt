package br.andrew.sap.events.listener

import br.andrew.sap.events.DraftOrderSalesSaveEvent
import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.OrdersService
import br.andrew.sap.services.TelegramRequestService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class OrderSalesSaveListener(val telegramRequest : TelegramRequestService,
                             val sapEnvrioment: SapEnvrioment,
                             val draftsService: DraftsService,
                             val ordersService: OrdersService) {
    @EventListener
    fun aplicaDesonerado(event: OrderSalesSaveEvent) {
        val order = ordersService.aplicaDesonerado(event.order)
        if(order is OrderSales)
            ordersService.update(order,order.docEntry.toString())
        else
            telegramRequest.send("O documento não é uma instancia de OrderSales entry: ${order.docEntry}")
    }

    @EventListener
    fun draftOrder(event: DraftOrderSalesSaveEvent) {
        if(event.order.docEntry == null) {
            val msg = "Erro ao atualizar um rascunho no procedimento de autorização, docEntry null - Num = ${event.order.docNum}"
            telegramRequest.send(msg)
            throw Exception(msg)
        }
        val draft = draftsService.getById(event.order.docEntry!!).tryGetValue<Document>()
        val order = ordersService.aplicaDesonerado(draft)
        draftsService.update(order,order.docEntry.toString())
    }
}