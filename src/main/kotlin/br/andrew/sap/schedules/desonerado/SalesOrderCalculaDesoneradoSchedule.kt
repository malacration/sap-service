package br.andrew.sap.schedules.desonerado

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.document.DesoneradoService
import br.andrew.sap.services.document.OrdersService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["jobs.quotation"], havingValue = "true", matchIfMissing = true)
class SalesOrderCalculaDesoneradoSchedule(
    val desoneradoService: DesoneradoService,
    val ordersService: OrdersService) {

    val logger: Logger = LoggerFactory.getLogger(SalesOrderCalculaDesoneradoSchedule::class.java)

    @Scheduled(fixedDelay = 15000)
    fun execute() {
        val filter = Filter(
            Predicate("U_pedido_update", "1", Condicao.EQUAL),
            Predicate("DocDate", "2024-09-30", Condicao.GREAT),
            Predicate("DocumentStatus", "bost_Open", Condicao.EQUAL),
        )
        val resultado = ordersService.get(filter).tryGetValues<Document>()
        resultado.forEach {
            try {
                val update = if(it.discountPercent == null || it.discountPercent!! == 0.0)
                    desoneradoService.aplicaDesonerado(it)
                else
                    FalhaAoCalcularDesonerado()
                ordersService.update(update,it.docEntry.toString())
            }catch (e : Throwable){
                logger.error("erro ao tentar cacular desonerado. DocNum "+it.docNum,e)
            }
        }
    }
}