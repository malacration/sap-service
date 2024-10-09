package br.andrew.sap.schedules.futura

import JournalEntry
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.InternalReconciliationsBuilder
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.base.adiantamento.ApropriacaoAdiantamento
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.InternalReconciliationsService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
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
@ConditionalOnProperty(value = [
    "venda-futura.adiantamento-item",
    "venda-futura.conta-controle"], matchIfMissing = false)
class ConciliacaoVendaFuturaSchedule(
    val adiantamentoService : DownPaymentService,
    val journalEntriesService : JournalEntriesService,
    val internalReconciliationsService: InternalReconciliationsService,
    val inoviceService : InvoiceService,
    @Value("\${venda-futura.adiantamento-item}") val itemConciliacaoVendaFutura : String,
    @Value("\${venda-futura.sequencia_adiantamento}") val sequenceCode : Int,
    @Value("\${venda-futura.conta-controle}") val contaControle : String) {

    val logger: Logger = LoggerFactory.getLogger(ConciliacaoVendaFuturaSchedule::class.java)


    @Scheduled(fixedDelay = 3000)
    fun execute() {
        val filterReclassificacaoEntrega = Filter(
            Predicate("TransactionCode", TransactionCodeTypes.VFET, Condicao.EQUAL),
            Predicate("TaxDate", "2024-10-02", Condicao.GREAT),
        )

        journalEntriesService.get(filterReclassificacaoEntrega).tryGetValues<JournalEntry>().forEach { journalReclassificado ->
            val ref = journalReclassificado.Reference?.toIntOrNull() ?: throw Exception("Nao tem numero de referencia. ${journalReclassificado.JdtNum}")
            val invoiceFilter = Filter(
                Predicate("U_venda_futura", 0, Condicao.GREAT),
                Predicate("DocNum", ref, Condicao.EQUAL)
            )
            inoviceService.get(invoiceFilter).tryGetValues<Invoice>().forEach { invoice ->

                val adiantamentos = adiantamentoService.adiantamentosAbertos(invoice)

                val adiantamentosDisponiveis = ApropriacaoAdiantamento(invoice, adiantamentos).get()
                if (adiantamentosDisponiveis.isNotEmpty()) {
                    val invoiceApropiacao = Invoice(
                        invoice.CardCode, null,
                        listOf(Product(itemConciliacaoVendaFutura, "1", "0", 9).also {
                            it.U_preco_base = 1.0
                        }),
                        invoice.getBPL_IDAssignedToInvoice()
                    )
                        .also {
                            it.downPaymentsToDraw = adiantamentosDisponiveis
                            it.U_venda_futura = invoice.U_venda_futura
                            it.controlAccount = contaControle
                            it.SequenceCode = sequenceCode
                            it.salesPersonCode = invoice.salesPersonCode
                        }

                    val apropriado = inoviceService
                        .save(invoiceApropiacao)
                        .tryGetValue<Document>()

                    internalReconciliationsService.save(
                        InternalReconciliationsBuilder(
                            journalReclassificado,
                            apropriado,
                            invoice.DocTotal?.toDoubleOrNull() ?: throw Exception("Documento sem total adequado")
                        ).setDebitTransRowId(1).build()
                    )
                }
            }
        }

    }
}

class Soma(var soma : BigDecimal?){

}