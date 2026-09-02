package br.andrew.sap.schedules.desonerado

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.impostos.ImpostosDesonerados
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.documents.DesoneradoService
import br.andrew.sap.services.documents.QuotationsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["jobs.quotation"], havingValue = "true", matchIfMissing = true)
class QuotationCalculaDesoneradoSchedule(
    val desoneradoService: DesoneradoService,
    val quotationService : QuotationsService,
    val impostos: ImpostosDesonerados) {

    val logger: Logger = LoggerFactory.getLogger(QuotationCalculaDesoneradoSchedule::class.java)

    @Scheduled(fixedDelay = 15000)
    fun execute() {
        val predicados = mutableListOf(
            Predicate("U_pedido_update", "1", Condicao.EQUAL),
            Predicate("DocDate", "2023-07-01", Condicao.GREAT),
            Predicate("DocumentStatus", "bost_Open", Condicao.EQUAL),
        )
        //so as filiais que calculam desonerado - as demais nem sao trazidas do SAP
        predicados.add(impostos.filtroFiliais())
        val resultado = quotationService.get(Filter(predicados)).tryGetValues<Document>()
        resultado.forEach {
            try {
                val update = if(it.discountPercent == null || it.discountPercent!! == 0.0)
                    desoneradoService.aplicaDesonerado(it)
                else
                    FalhaAoCalcularDesonerado()
                quotationService.update(update,it.docEntry.toString())
            }catch (e : Throwable){
                logger.error("erro ao tentar cacular desonerado. DocNum "+it.docNum,e)
            }
        }
    }
}