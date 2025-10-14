package br.andrew.sap.schedules.futura

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.payment.HandlePaymentTermsLines
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.self.vendafutura.ContratoParse
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.OrdersService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.invent.OrigemBoletoEnum
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
@Profile("!test")
@ConditionalOnProperty(value = ["venda-futura.utilizacao"], matchIfMissing = false)
class VendaFuturaScheduled(
    protected val authService: AuthService,
    val sqlQueriesService: SqlQueriesService,
    val contratoService : ContratoVendaFuturaService,
    val paymentService : PaymentTermsTypesService,
    val adiantamentoService : DownPaymentService,
    val bankplus : BankPlusService,
    val orderService : OrdersService,
    @Value("\${venda-futura.utilizacao:-1}") val idUtilizacao : Long,
    @Value("\${venda-futura.filiais:-2}") val filiais : List<Int>,
    @Value("\${venda-futura.carencia:7}") val carenciaDias : Int,
    protected val env: SapEnvrioment) {

    val logger: Logger = LoggerFactory.getLogger(VendaFuturaScheduled::class.java)


    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    fun execute() {
        filiais.forEach { filial ->
        sqlQueriesService.execute(
            "vendafutura-aberto.sql",
            listOf(Parameter("utilizacao",idUtilizacao),
            Parameter("filiais",filial),
            Parameter("startDate","2024-12-09"))
        )?.tryGetValues<DocEntry>()?.forEach {
            orderService.get(Filter("DocEntry",it.DocEntry!!,Condicao.EQUAL))
                .tryGetValues<Document>().forEach { order ->
                    contratoService.saveOnly(ContratoParse.parse(order))
                    orderService.close(order.docEntry.toString())
                }
            }}
    }
}