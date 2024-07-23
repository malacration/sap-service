package br.andrew.sap.events.listener

import br.andrew.sap.model.sap.ErrorMsg
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.SapError
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.exceptions.BusinessPartnerNotAssignedException
import br.andrew.sap.model.exceptions.PixPaymentException
import br.andrew.sap.model.exceptions.SapGenericException
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.TelegramRequestService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SapErrorListener(val telegramRequest : TelegramRequestService,
                       val sapEnvrioment: SapEnvrioment,
                       val bussinesPartenerService : BusinessPartnersService,
                       val ordersService: OrdersService
) {

    @EventListener
    fun sapExceptiopn(event: SapGenericException) {
        erroSap(event.erro)
    }


    @EventListener
    fun erroSap(sapErro: SapError) {
        val error = sapErro.error
        val detail = sapErro.entry.toString()
        val msg = "Erro no SAP-Service ao chamar o SAP o servidor diz: ${error.code} ${error.message.value} - [Base:${sapEnvrioment.companyDB}] - ${detail}"
        telegramRequest.send(msg,TipoMensagem.erros)
    }

//    @EventListener
    //TODO disable logs de erros, depois ativo?
    fun error(error: ErrorMsg) {
        val msg = "Erro no SAP-Service ao chamar o SAP o servidor diz: ${error.code} ${error.message.value} - [Base:${sapEnvrioment.companyDB}]"
        telegramRequest.send(msg,TipoMensagem.erros)
    }

    @EventListener
    fun bussinesPartnerNotAssingned(error: BusinessPartnerNotAssignedException) {
        val msg = "Erro no SAP-Service ao cadastrar pedido de venda! o SAP o servidor " +
                "diz: ${error.erro.error.code} ${error.erro.error.message.value} - [Base:${sapEnvrioment.companyDB}]" +
                "\nO sistema ira vincular o cliente automaticamente para essa empresa, por favor aguarde alguns minutos"
        telegramRequest.send(msg,TipoMensagem.erros)
        try{
            bussinesPartenerService.addBusinesPlace(error.entry.CardCode, error.entry.getBPL_IDAssignedToInvoice())
        }catch (e: Exception){
            val msg = "Erro ao vincular o cliente[${error.entry.CardCode}] automaticamente para essa empresa[${error.entry.getBPL_IDAssignedToInvoice()}], por favor vincule manualmente "
            telegramRequest.send(msg,TipoMensagem.erros)
        }

    }

    @EventListener
    fun linkedPaymentMethodException(erro : PixPaymentException){
        val entry = erro.erro.entry
        if(entry is Document)
            bussinesPartenerService.addPaymentMethod(entry.CardCode,entry.paymentMethod!!)
    }


}