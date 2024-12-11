package br.andrew.sap.schedules

import br.andrew.sap.controllers.RdStationController
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.TelegramRequestService
import br.andrew.sap.services.document.OrdersService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate


@Component
@ConditionalOnProperty(value =["rd.enable"], havingValue = "true", matchIfMissing = false)
class SendOrderToEventToRdStation(
    val controller : RdStationController,
    val ordersService: OrdersService,
    @Value("\${rd.consumidor-final:CLI0003676}") val consumidorFinal : String,
    @Value("\${rd.dias:10}") val dias : Long,
    @Value("\${rd.sap-id-filiais:2,4,11,17,18}") val filiais : List<Int>,
    val telegramRequestService: TelegramRequestService) {

    val logger: Logger = LoggerFactory.getLogger(SendOrderToEventToRdStation::class.java)



    @Scheduled(fixedDelay = 30000)
    fun execute() {
        val data = LocalDate.now().plusDays(-dias).toString()
        val predicados = mutableListOf(
            Predicate("DocDate", data, Condicao.GREAT),
            Predicate("U_rd_station", "null", Condicao.EQUAL),
            Predicate("CardCode", "CLI0003676", Condicao.NOT_EQUAL),
            Predicate("DocumentStatus", DocumentStatus.bost_Open,Condicao.EQUAL),
            Predicate("BPL_IDAssignedToInvoice", filiais, Condicao.IN),
        )
        ordersService.get(Filter(predicados)).tryGetValues<OrderSales>().forEach{
            try{
                controller.getByCodParceiro(it)
                ordersService.update("{ \"U_rd_station\": \"yes\"}","${it.docEntry}")
                logger.info("Pedido - DocNum: ${it.docNum} - Enviado ao RdStation")
                telegramRequestService.send("Pedido - DocNum: ${it.docNum} - Enviado ao RdStation",TipoMensagem.eventos)
            }catch (e : Exception){
                logger.error("Erro ao enviar pedido para RD-Station - DocNum: ${it.docNum}",e)
                telegramRequestService.send("Erro ao enviar pedido para RD-Station - DocNum: ${it.docNum}",TipoMensagem.erros)
            }
        }
    }
}