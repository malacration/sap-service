package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.User
import br.andrew.sap.model.documents.Document
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.UserService
import br.andrew.sap.services.document.DesoneradoService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class DraftCalculaDesoneradoSchedule(
    val desoneradoService: DesoneradoService,
    val draftService : DraftsService,val currentUser : User
) {

    val logger: Logger = LoggerFactory.getLogger(DraftCalculaDesoneradoSchedule::class.java)

    @Scheduled(fixedDelay = 15000)
    fun execute() {
        val filter = Filter(
            Predicate("U_pedido_update", "1", Condicao.EQUAL),
            Predicate("DocDate", "2023-07-01", Condicao.GREAT),
            Predicate("DocumentStatus", "bost_Open", Condicao.EQUAL),
            Predicate("UserSign", currentUser.internalKey, Condicao.NOT_EQUAL),
        )
        val resultado = draftService.get(filter).tryGetValues<Document>()
        resultado.forEach {
            val update = if(it.discountPercent == null || it.discountPercent!! == 0.0)
                desoneradoService.aplicaDesonerado(it)
            else
                FalhaAoCalcularDesonerado()
            draftService.update(update,it.docEntry.toString())
        }
    }
}