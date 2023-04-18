package br.andrew.sap.listener

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.services.TelegramRequestService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class OrderSalesSaveListener(val telegramRequest : TelegramRequestService) {

    @EventListener
    fun onApplicationEvent(event: OrderSalesSaveEvent) {
        println("Teste")
        telegramRequest.send("mensagem")
    }
}