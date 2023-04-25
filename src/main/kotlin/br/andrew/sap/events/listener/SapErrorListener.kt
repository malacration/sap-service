package br.andrew.sap.events.listener

import br.andrew.sap.model.ErrorMsg
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.SapError
import br.andrew.sap.model.documents.Document
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
    fun erroSap(sapErro: SapError) {
        val error = sapErro.error
        val detail = sapErro.entry.toString()
        val msg = "Erro no SAP-Service ao chamar o SAP o servidor diz: ${error.code} ${error.message.value} - [Base:${sapEnvrioment.companyDB}] - ${detail}"
        telegramRequest.send(msg)
    }

    @EventListener
    fun error(error: ErrorMsg) {
        val msg = "Erro no SAP-Service ao chamar o SAP o servidor diz: ${error.code} ${error.message.value} - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg)

    }
}