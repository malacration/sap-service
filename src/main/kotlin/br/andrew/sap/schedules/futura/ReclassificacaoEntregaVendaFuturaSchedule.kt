package br.andrew.sap.schedules.futura

import JournalEntry
import JournalEntryLines
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.InternalReconciliationsBuilder
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.InternalReconciliationsService
import br.andrew.sap.services.batch.BatchService
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
import java.util.concurrent.TimeUnit


@Component
@Profile("!test")
@ConditionalOnProperty(value = ["venda-futura.conta-controle"], matchIfMissing = false)
class ReclassificacaoEntregaVendaFuturaSchedule(
    protected val authService: AuthService,
    val bankplus : BankPlusService,
    val batchService: BatchService,
    val journalEntriesService : JournalEntriesService,
    val internalReconciliationsService: InternalReconciliationsService,
    val inoviceService : InvoiceService,
    @Value("\${venda-futura.filiais:-2}") val filiais : List<Int>,
    @Value("\${venda-futura.conta-controle}") val contaControle : String) {

    val logger: Logger = LoggerFactory.getLogger(ConciliacaoVendaFuturaSchedule::class.java)


    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    fun execute() {
        val entregasFilter = Filter(
            Predicate("U_venda_futura",0,Condicao.GREAT),
            Predicate("DocDate", "2024-11-05", Condicao.GREAT),
            Predicate("U_entrega_vf", "1", Condicao.EQUAL),
            Predicate("U_conciliar_automatico", "1", Condicao.EQUAL),
            Predicate("DocumentStatus",DocumentStatus.bost_Open,Condicao.EQUAL),
            Predicate("BPL_IDAssignedToInvoice", filiais, Condicao.IN),
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
                    JournalEntry(listOf(cred,deb),"Reclassificação entrega. Mercadoria venda futura [${invoice.U_venda_futura}]. NF Num ${invoice.docNum}").also {
                        it.TransactionCode = TransactionCodeTypes.VFET.toString()
                        it.Reference = invoice.docNum
                    })
            internalReconciliationsService.save(
                InternalReconciliationsBuilder(invoice,journalEntrie,docTotal).build()
            )
        }
    }
}