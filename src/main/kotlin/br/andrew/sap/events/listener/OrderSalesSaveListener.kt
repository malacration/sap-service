package br.andrew.sap.events.listener

import br.andrew.sap.events.OrderSalesSaveEvent
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.TelegramRequestService
import br.andrew.sap.services.document.DesoneradoService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class OrderSalesSaveListener(val telegramRequest : TelegramRequestService,
                             val sapEnvrioment: SapEnvrioment,
                             val draftsService: DraftsService,
                             val ordersService: OrdersService,
                             val desoneradoService: DesoneradoService
) {
    @EventListener
    fun aplicaDesonerado(event: OrderSalesSaveEvent) {
        val order = desoneradoService.aplicaDesonerado(event.order)
        if(order is OrderSales)
            ordersService.update(order,order.docEntry.toString())
        else
            telegramRequest.send("O documento não é uma instancia de OrderSales entry: ${order.docEntry}", TipoMensagem.erros)
    }

    @EventListener
    fun draftOrder(event: CreditException) {
        val draft = draftsService.getById(event.idLocation).tryGetValue<Document>()
        val order = desoneradoService.aplicaDesonerado(draft)
        draftsService.update(order,order.docEntry.toString())
        val msg = "Rascunho do pedido para ${order.cardName ?: "semCardName"} [DocNum:${order.docNum ?: "Sem docNum"}] foi recebido com sucesso! " +
                "Porem esta em um procedimento de autorização - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg,TipoMensagem.autorizacao)
    }
}