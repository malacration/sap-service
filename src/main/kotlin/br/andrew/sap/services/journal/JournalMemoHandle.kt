package br.andrew.sap.services.journal

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

    fun atualizarGrupoEconomicoECentroCusto(journalId: Int) {
        val lancamento = journalEntryService.getByDocEntry(journalId)
            ?: throw Exception("Não encontrou JournalEntry para o docEntry $journalId")

        val docEntryOriginal = lancamento.Original
            ?: throw Exception("Campo 'Original' está nulo para o JournalEntry $journalId")

        when (lancamento.OriginalJournal) {
            "ttVendorPayment" -> {
                val pagamento = vendorPaymentService.getById(docEntryOriginal)
                val docEntryNota = extrairDocEntryNota(pagamento, docEntryOriginal)
                    ?: throw Exception("Não foi possível obter o DocEntry da nota fiscal em 'PaymentInvoices'. no $docEntryOriginal")

                val grupo = purchaseInvoiceService.getCostingCode(docEntryNota, "CostingCode")
                    ?: throw Exception("Grupo Econômico não encontrado na nota fiscal $docEntryNota")
                val centro = purchaseInvoiceService.getCostingCode(docEntryNota, "CostingCode2")
                    ?: throw Exception("Centro de Custo não encontrado na nota fiscal $docEntryNota")

                journalEntryService.updateGrupoEconomicoJournalEntry(journalId, grupo, centro)
            }

            "ttReceipt" -> {
                val pagamento = incomingPaymentService.getById(docEntryOriginal)
                val docEntryNota = extrairDocEntryNota(pagamento, docEntryOriginal)
                    ?: throw Exception(
                        "Não foi possível obter o DocEntry da nota fiscal em 'PaymentInvoices' no $docEntryOriginal")

                val grupo = invoiceService.getCostingCodeInvoice(docEntryNota, "CostingCode")
                    ?: throw Exception("Grupo Econômico não encontrado na nota fiscal $docEntryNota")
                val centro = invoiceService.getCostingCodeInvoice(docEntryNota, "CostingCode2")
                    ?: throw Exception("Centro de Custo não encontrado na nota fiscal $docEntryNota")

                journalEntryService.updateGrupoEconomicoJournalEntry(journalId, grupo, centro)
            }
            else -> {
                journalEntryService.markGrupoEconomicoChecked(journalId)
            }
        }
    }

    private fun extrairDocEntryNota(pagamento: Map<String, Any?>, docEntryOriginal: Int): Int? {
        return (pagamento["PaymentInvoices"] as? List<*>)?.firstOrNull()?.let { invoice ->
            (invoice as? Map<*, *>)?.get("DocEntry") as? Int
        }
    }
}