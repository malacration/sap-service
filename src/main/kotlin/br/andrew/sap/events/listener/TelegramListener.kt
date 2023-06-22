package br.andrew.sap.events.listener

import br.andrew.sap.events.DraftOrderSalesSaveEvent
import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.TelegramRequestService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TelegramListener(val telegramRequest : TelegramRequestService,
                       val sapEnvrioment: SapEnvrioment,
                       val draftsService: DraftsService,
                       val ordersService: OrdersService
) {

    @EventListener
    fun mensagemTelegram(event: OrderSalesSaveEvent) {
        val entity = event.order
        val msg = "Pedido para ${entity.cardName ?: ""} [DocNum:${entity.docNum}] foi recebido com sucesso! - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg)
    }

    @EventListener
    fun notificaDraftCreate(event: DraftOrderSalesSaveEvent) {
        val entity = event.order
        val msg = "Pedido para ${entity.cardName ?: ""} [DocNum:${entity.docNum}] foi recebido com sucesso! " +
                "Porem esta em um procedimento de autorização - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg)

    }
}