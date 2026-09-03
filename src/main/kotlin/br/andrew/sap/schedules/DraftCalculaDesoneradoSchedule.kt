package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.sistema.SapUser
import br.andrew.sap.model.impostos.ImpostosDesonerados
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.schedules.desonerado.FalhaAoCalcularDesonerado
import br.andrew.sap.services.financeiro.DraftsService
import br.andrew.sap.services.documents.DesoneradoService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate


@Component
@ConditionalOnProperty(value = ["jobs.draft"], havingValue = "true", matchIfMissing = false)
class DraftCalculaDesoneradoSchedule(
    @Value("\${draft.dias:10}") val dias : Long,
    val desoneradoService: DesoneradoService,
    val draftService : DraftsService,val currentSapUser : SapUser,
    val impostos: ImpostosDesonerados
) {

    val logger: Logger = LoggerFactory.getLogger(DraftCalculaDesoneradoSchedule::class.java)

    @Scheduled(fixedDelay = 15000)
    fun execute() {
        val data = LocalDate.now().plusDays(-dias).toString()
        val predicados = mutableListOf(
            Predicate("U_pedido_update", "1", Condicao.EQUAL),
            Predicate("DocDate", data, Condicao.GREAT),
            Predicate("DocumentStatus", "bost_Open", Condicao.EQUAL),
            Predicate("UserSign", currentSapUser.internalKey, Condicao.NOT_EQUAL),
            Predicate("DocObjectCode", "oOrders", Condicao.EQUAL),
        )
        //so as filiais que calculam desonerado - as demais nem sao trazidas do SAP
        predicados.add(impostos.filtroFiliais())
        val resultado = draftService.get(Filter(predicados)).tryGetValues<Document>()
        resultado.forEach {
            val update = if(it.discountPercent == null || it.discountPercent!! == 0.0)
                desoneradoService.aplicaDesonerado(it, rascunho = true)
            else
                FalhaAoCalcularDesonerado()
            draftService.update(update,it.docEntry.toString())
        }
    }
}