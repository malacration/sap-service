package br.andrew.sap.services.journal

import JournalEntry
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.journal.OriginalJournal
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.bank.IncomingPaymentService
import org.springframework.stereotype.Service
import br.andrew.sap.services.bank.VendorPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.document.PurchaseInvoiceService
import okhttp3.internal.concurrent.TaskRunner.Companion.logger

@Service
class JournalMemoHandle(
    val journalEntryService: JournalEntriesService,
    val purchaseInvoiceService: PurchaseInvoiceService,
    val vendorPaymentService: VendorPaymentService,
    val incomingPaymentService: IncomingPaymentService,
    val invoiceService: InvoiceService,
    val services: List<ServiceOriginalJournal>){

    fun updateMemoJournal(idJournal: Int) {
        val journalEntry = journalEntryService.getByDocEntry(idJournal)

        journalEntry?.Original?.let { original ->
            val service = services.firstOrNull {
                it.getOriginalJournal().toString() == journalEntry.OriginalJournal
            }
            if (service != null) {
                val memo = service.getEntryOriginalJournal(original).getMemoForJournal()
                journalEntryService.updateMemoJournalEntry(idJournal, memo)
            }else{
                journalEntryService.markMemoChecked(idJournal)
            }

        }
    }

    fun atribuiCentroCustoEmContasRecebeOuPagar(lancamento: JournalEntry): JournalEntry {
        val docEntryOriginal = lancamento.Original
            ?: throw Exception("Campo 'Original' está nulo para o JournalEntry ${lancamento.JdtNum}")

        val services : Pair<EntitiesService<Payment>, EntitiesService<*>> =  when (lancamento.OriginalJournal) {
            OriginalJournal.ttVendorPayment.toString() -> Pair(vendorPaymentService,purchaseInvoiceService)
            OriginalJournal.ttReceipt.toString() -> Pair(incomingPaymentService,invoiceService)
            else -> throw IllegalArgumentException("O registro precisa ser de origem de um dos valores esperados.")
        }
        val paymentService = services.first
        val documentService = services.second
        val payment : Payment = paymentService.getById(docEntryOriginal)
            .tryGetValue<Payment>()

        val paymentDocEntry : Int = payment.paymentInvoices.firstOrNull()?.docEntry
            ?: throw IllegalArgumentException("O pagamento relacionado no lancamento contabil deve existir")

        val document = documentService.getById(paymentDocEntry).tryGetValue<Document>()
        lancamento.costingCodes(document)
        return lancamento
    }
}