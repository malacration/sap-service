package br.andrew.sap.schedules.futura

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.InternalReconciliationsBuilder
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.base.adiantamento.ApropriacaoAdiantamento
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.InternalReconciliationsService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.invent.BankPlusService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal


@Component
@Profile("!test")
@ConditionalOnProperty(value = ["venda-futura.utilizacao"], matchIfMissing = false)
class ConciliacaoVendaFuturaSchedule(
    protected val authService: AuthService,
    val adiantamentoService : DownPaymentService,
    val bankplus : BankPlusService,
    val internalReconciliationsService: InternalReconciliationsService,
    val inoviceService : InvoiceService,
    @Value("\${venda-futura.item}") val itemConciliacaoVendaFutura : String,
    @Value("\${venda-futura.forma-pagamento:null}") val formaPagamento : String?,) {

    val logger: Logger = LoggerFactory.getLogger(ConciliacaoVendaFuturaSchedule::class.java)


    @Scheduled(fixedDelay = 3000)
    fun execute() {
        val entregasFilter = Filter(
            Predicate("U_venda_futura",0,Condicao.GREAT),
            Predicate("DocumentStatus",DocumentStatus.bost_Open,Condicao.EQUAL),
        )
        inoviceService.get(entregasFilter).tryGetValues<Invoice>().forEach { invoice ->

            val adiantamentos = adiantamentoService.adiantamentosAbertos(invoice)

            val adiantamentosDisponiveis = ApropriacaoAdiantamento(invoice,adiantamentos).get()
            if(adiantamentosDisponiveis.isNotEmpty()){
                val invoiceApropiacao = Invoice(
                    invoice.CardCode,null,
                    listOf(Product(itemConciliacaoVendaFutura,"1","0",9)),
                    invoice.getBPL_IDAssignedToInvoice())
                    .also {
                        it.downPaymentsToDraw = adiantamentosDisponiveis
                        it.U_venda_futura = invoice.U_venda_futura
                        it.paymentMethod = formaPagamento
                    }

                val apropriado = inoviceService.save(invoiceApropiacao).tryGetValue<Document>()
                internalReconciliationsService.save(
                    InternalReconciliationsBuilder(invoice,apropriado).build()
                )
            }
        }
    }
}

class Soma(var soma : BigDecimal?){

}