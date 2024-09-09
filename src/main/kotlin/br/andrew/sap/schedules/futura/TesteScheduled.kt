package br.andrew.sap.schedules.futura

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.InternalReconciliationsBuilder
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.base.adiantamento.ApropriacaoAdiantamento
import br.andrew.sap.model.sap.documents.base.adiantamento.DownPaymentsToDraw
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.InternalReconciliationsService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.document.OrdersService
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
class TesteScheduled(
    protected val authService: AuthService,
    val sqlQueriesService: SqlQueriesService,
    val contratoService : ContratoVendaFuturaService,
    val paymentService : PaymentTermsTypesService,
    val adiantamentoService : DownPaymentService,
    val incomingPaymentService : IncomingPaymentService,
    val bankplus : BankPlusService,
    val orderService : OrdersService,
    val internalReconciliationsService: InternalReconciliationsService,
    val inoviceService : InvoiceService,
    protected val env: SapEnvrioment) {

    val logger: Logger = LoggerFactory.getLogger(TesteScheduled::class.java)


    @Scheduled(fixedDelay = 3000)
    fun execute() {
        //23316
        // cardCode CardCode -> CLI0003170
        var docNum = 23314 //TODO remover
        //TODO adicionar forma de pagamento especifica parae essa gambi
        val itemAdiantamento = "FIN0000002" //TODO adicionar parametro
        val entregasFilter = Filter(
            Predicate("U_venda_futura",0,Condicao.GREAT),
            Predicate("DocumentStatus",DocumentStatus.bost_Open,Condicao.EQUAL),

            Predicate("DocNum",docNum,Condicao.EQUAL) //TODO remover linha
        )

        inoviceService.get(entregasFilter).tryGetValues<Invoice>().forEach { invoice ->
            val exp = Exception("Nao foi possivel buscar o adianemtno, entrega de venda sem id contrato")
            val adiantamentoFilter = Filter(
                Predicate("U_venda_futura",invoice.U_venda_futura?: throw exp,Condicao.EQUAL),
                Predicate("CardCode",invoice.CardCode,Condicao.EQUAL),
                Predicate("DownPaymentStatus","so_Open",Condicao.EQUAL),
                Predicate("DocumentStatus",DocumentStatus.bost_Close,Condicao.EQUAL),
                //Talvez DownPaymentStatus indique se tem saldo a apropriar ou nao
            )
            val adiantamentos : List<DownPayment> = adiantamentoService.get(adiantamentoFilter).tryGetValues<DownPayment>().map{
                val apropriacoes : List<Soma> = sqlQueriesService
                    .execute("adiantamento-apropriado.sql", Parameter("docEntry",it.docEntry!!))
                    ?.tryGetValues<Soma>() ?: listOf(Soma(BigDecimal.ZERO))
                if(apropriacoes.size > 1)
                    throw Exception("Nao pode ter mais de uma apropriacao para um adiantamento")
                it.apropriado = apropriacoes.firstOrNull()?.soma ?: BigDecimal.ZERO
                it
            }

            val downDraw = ApropriacaoAdiantamento(invoice,adiantamentos).get()

            val invoiceApropiacao = Invoice(
                    invoice.CardCode,null,
                    listOf(Product(itemAdiantamento,"1","0",9)),
                    invoice.getBPL_IDAssignedToInvoice())
                .also {
                    it.downPaymentsToDraw = downDraw
                    it.U_venda_futura = invoice.U_venda_futura
                }
            val apropriado = inoviceService.save(invoiceApropiacao).tryGetValue<Document>()

            internalReconciliationsService.save(
                InternalReconciliationsBuilder(invoice,apropriado).build()
            )

            //TODO falta reconciliacao interna!
            //incomingPaymentService.save(payment)

        }
        // PaidToDate valor pago ate a data atual
        val adiantamento = adiantamentoService.getById(3808)
    }
}

class Soma(var soma : BigDecimal?){

}