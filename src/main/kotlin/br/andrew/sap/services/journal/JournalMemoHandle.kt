package br.andrew.sap.services.journal

import br.andrew.sap.services.document.PurchaseInvoiceService
import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import org.springframework.stereotype.Service

@Service
class JournalMemoHandle(
    val journalEntryService: JournalEntriesService,
    val purchaseInvoiceService: PurchaseInvoiceService,
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

    fun updateGrupoEconomicoCentroCusto(idJournal: Int) {
        val journalEntry = journalEntryService.getByDocEntry(idJournal)

        journalEntry?.Original?.let {
            val isRelevantService = journalEntry.OriginalJournal == "ttVendorPayment" || journalEntry.OriginalJournal == "ttReceipt"

            if (isRelevantService) {
                val reference = journalEntry.Reference ?: return
                val costingCode = purchaseInvoiceService.getCostingCodeFromPurchaseInvoice(reference.toInt())
                val centrodeCusto = purchaseInvoiceService.getCostingCode2FromPurchaseInvoice(reference.toInt())

                if (costingCode != null && centrodeCusto != null) {
                    journalEntryService.updateGrupoEconomicoJournalEntry(idJournal, costingCode, centrodeCusto)
                } else {
                    logger.warning("CostingCode ou Centro de Custo não encontrado para o Reference: $reference")
                }
            } else {
                journalEntryService.markGrupoEconomicoChecked(idJournal)
            }
        }
    }
}