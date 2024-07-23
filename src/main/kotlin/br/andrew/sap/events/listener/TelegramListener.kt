package br.andrew.sap.events.listener

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.TelegramRequestService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TelegramListener(val telegramRequest : TelegramRequestService,
                       val sapEnvrioment: SapEnvrioment,
) {

    @EventListener
    fun mensagemTelegram(event: OrderSalesSaveEvent) {
        val entity = event.order
        val msg = "Pedido para ${entity.cardName ?: ""} [DocNum:${entity.docNum}] foi recebido com sucesso! - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg,TipoMensagem.eventos)
    }

    @EventListener
    fun mensagemTelegram(entity: Document) {
        val msg = "Pedido para ${entity.cardName ?: ""} [DocNum:${entity.docNum}] foi recebido com sucesso! - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg,TipoMensagem.eventos)
    }
}