package br.andrew.sap.events.listener

import br.andrew.sap.model.Error
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.SapError
import br.andrew.sap.model.exceptions.SapGenericException
import br.andrew.sap.services.OrdersService
import br.andrew.sap.services.TelegramRequestService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SapErrorListener(val telegramRequest : TelegramRequestService,
                       val sapEnvrioment: SapEnvrioment,
                       val ordersService: OrdersService) {

    @EventListener
    fun sapExceptiopn(event: SapGenericException) {
        erroSap(event.erro)
    }


    @EventListener
    fun erroSap(error: SapError) {
        error(error.error)
    }

    @EventListener
    fun error(error: Error) {
        val msg = "Erro no SAP-Service ao chamar o SAP o servidor diz: ${error.code} ${error.message.value} - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg)
    }
}