package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.services.document.DesoneradoService
import br.andrew.sap.services.document.QuotationsService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["jobs.quotation"], havingValue = "true", matchIfMissing = true)
class QuotationCalculaDesoneradoSchedule(
    val desoneradoService: DesoneradoService,
    val quotationService : QuotationsService) {

    val logger: Logger = LoggerFactory.getLogger(DraftCalculaDesoneradoSchedule::class.java)

    @Scheduled(fixedDelay = 15000)
    fun execute() {
        val filter = Filter(
            Predicate("U_pedido_update", "1", Condicao.EQUAL),
            Predicate("DocDate", "2023-07-01", Condicao.GREAT),
            Predicate("DocumentStatus", "bost_Open", Condicao.EQUAL),
        )
        val resultado = quotationService.get(filter).tryGetValues<Document>()
        resultado.forEach {
            val update = if(it.discountPercent == null || it.discountPercent!! == 0.0)
                desoneradoService.aplicaDesonerado(it)
            else
                FalhaAoCalcularDesonerado()
            quotationService.update(update,it.docEntry.toString())
        }
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FalhaAoCalcularDesonerado(){
    var comments = "O desconto precisa ser removido para o calculo funcionar adequadamente"
    var u_pedido_update = "0"
}