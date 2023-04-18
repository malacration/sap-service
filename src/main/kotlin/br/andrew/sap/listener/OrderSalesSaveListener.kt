package br.andrew.sap.listener

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.services.OrdersService
import br.andrew.sap.services.TelegramRequestService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class OrderSalesSaveListener(val telegramRequest : TelegramRequestService,
                             val sapEnvrioment: SapEnvrioment,
                             val ordersService: OrdersService) {

    @EventListener
    fun mensagemTelegram(event: OrderSalesSaveEvent) {
        val entity = event.order
        val msg = "Pedido para ${entity.cardName ?: ""} [DocNum:${entity.docNum}] foi recebido com sucesso! - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg)
    }

    @EventListener
    fun aplicaDesonerado(event: OrderSalesSaveEvent) {
        val order = event.order
        ordersService.aplicaDesonerado(order)
    }
}