package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.User
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.approval.ApprovalRequestsService
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.document.DesoneradoService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.document.QuotationsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class QuotationCalculaDesoneradoSchedule(
    val desoneradoService: DesoneradoService,
    val quotationService : QuotationsService) {

    val logger: Logger = LoggerFactory.getLogger(QuotationCalculaDesoneradoSchedule::class.java)

    @Scheduled(fixedRate = 15000)
    fun execute() {
        val filter = Filter(
            Predicate("U_pedido_update", "1", Condicao.EQUAL),
            Predicate("DocDate", "2023-07-01", Condicao.GREAT),
            Predicate("DocumentStatus", "bost_Open", Condicao.EQUAL),
        )
        val resultado = quotationService.get(filter).tryGetValues<OrderSales>()
        resultado.forEach {
            quotationService.update(desoneradoService.aplicaDesonerado(it) as OrderSales,it.docEntry.toString())
        }
    }
}