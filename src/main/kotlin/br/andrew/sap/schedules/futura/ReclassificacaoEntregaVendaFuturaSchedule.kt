package br.andrew.sap.schedules.futura

import JournalEntry
import JournalEntryLines
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
@ConditionalOnProperty(value = ["venda-futura.conta-controle"], matchIfMissing = false)
class ReclassificacaoEntregaVendaFuturaSchedule(
    protected val authService: AuthService,
    val bankplus : BankPlusService,
    val journalEntriesService : JournalEntriesService,
    val internalReconciliationsService: InternalReconciliationsService,
    val inoviceService : InvoiceService,
    @Value("\${venda-futura.conta-controle}") val contaControle : String) {

    val logger: Logger = LoggerFactory.getLogger(ConciliacaoVendaFuturaSchedule::class.java)


    @Scheduled(fixedDelay = 3000)
    fun execute() {
        val entregasFilter = Filter(
            Predicate("U_venda_futura",0,Condicao.GREAT),
            Predicate("DocDate", "2024-10-02", Condicao.GREAT),
            Predicate("DocumentStatus",DocumentStatus.bost_Open,Condicao.EQUAL),
        )

        inoviceService.get(entregasFilter).tryGetValues<Invoice>().forEach { invoice ->
            val filial = invoice.getBPL_IDAssignedToInvoice()
            val docTotal = invoice.DocTotal?.toDoubleOrNull() ?: throw Exception("valor Documento nao e valido")
            val cred = JournalEntryLines(
                invoice.controlAccount?: throw Exception("Lancamento sem conta controle"),
                0.0,docTotal,
                filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")).also {
                it.ShortName = invoice.CardCode
            }
            val deb = JournalEntryLines(contaControle,docTotal,0.0,filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")).also {
                it.ShortName = invoice.CardCode
            }

            val journalEntrie = journalEntriesService
                .saveOrRecouverReference(
                    JournalEntry(listOf(cred,deb),"Reclassificação entrega de mercadoria venda futura").also {
                        it.TransactionCode = TransactionCodeTypes.VFET.toString()
                        it.Reference = invoice.docNum
                    })

            internalReconciliationsService.save(
                InternalReconciliationsBuilder(invoice,journalEntrie,docTotal).build()
            )
        }
    }
}