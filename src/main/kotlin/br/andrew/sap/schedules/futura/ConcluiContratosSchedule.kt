package br.andrew.sap.schedules.futura

import br.andrew.sap.controllers.ContratoVendaFuturaController
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Status
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


@Component
@Profile("!test")
@ConditionalOnProperty(value = [
    "venda-futura.adiantamento-item",
    "venda-futura.conta-controle"], matchIfMissing = false)
class ConcluiContratosSchedule(
    val sqlQueriesService : SqlQueriesService,
    val contratoService : ContratoVendaFuturaService,
    val controller : ContratoVendaFuturaController,
    @Value("\${venda-futura.adiantamento-item}") val itemConciliacaoVendaFutura : String,
    @Value("\${venda-futura.filiais:-2}") val filiais : List<Int>,
    @Value("\${venda-futura.sequencia_adiantamento}") val sequenceCode : Int,
    @Value("\${venda-futura.utilizacao.baixa:9}") val usage : Int,
    @Value("\${venda-futura.conta-controle}") val contaControle : String) {

    val logger: Logger = LoggerFactory.getLogger(ConcluiContratosSchedule::class.java)


    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    fun execute() {
        var contratosIds : NextLink<DocEntry>? = null
        do {
            contratosIds = if(contratosIds == null)
                sqlQueriesService.execute("para-finalizar.sql")?.tryGetNextValues<DocEntry>()
            else
                sqlQueriesService.nextLink(contratosIds.nextLink)?.tryGetNextValues<DocEntry>()

            contratosIds?.content?.forEach {
                val contrato : Contrato = contratoService.getById(it.DocEntry!!).tryGetValue<Contrato>()
                val entregas = controller.entregas(contrato.DocEntry!!)
                if(contrato.tudoEntregue(entregas)){
                    contrato.U_status = Status.concluido
                    contratoService.update(contrato,contrato.DocEntry.toString())
                }
            }
        }while (contratosIds?.hasNext() ?: false)

    }
}

class Soma(var soma : BigDecimal?){

}