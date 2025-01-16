package br.andrew.sap.services.journal

import br.andrew.sap.services.bank.VendorPaymentService
import br.andrew.sap.services.document.PurchaseInvoiceService
import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import org.springframework.stereotype.Service

@Service
class JournalMemoHandle(
    val journalEntryService: JournalEntriesService,
    val purchaseInvoiceService: PurchaseInvoiceService,
    val vendorPaymentService: VendorPaymentService,
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
        val lancamento = journalEntryService.getByDocEntry(journalId) ?: return
        val docEntryOriginal = lancamento.Original ?: return

        if (lancamento.OriginalJournal in listOf("ttVendorPayment", "ttReceipt")) {
            val pagamento = vendorPaymentService.getById(docEntryOriginal)
            val docEntryNota = extrairDocEntryNota(pagamento, docEntryOriginal) ?: return

            val grupoEconomico = purchaseInvoiceService.getCostingCode(docEntryNota, "CostingCode")
            val centroDeCusto = purchaseInvoiceService.getCostingCode(docEntryNota, "CostingCode2")

            if (grupoEconomico != null && centroDeCusto != null) {
                journalEntryService.updateGrupoEconomicoJournalEntry(journalId, grupoEconomico, centroDeCusto)
            } else {
                logger.warning("Grupo Econômico ou Centro de Custo não encontrado para a nota fiscal com referência: ${lancamento.Reference}")
            }
        } else {
            journalEntryService.markGrupoEconomicoChecked(journalId)
        }
    }

    private fun extrairDocEntryNota(pagamento: Map<String, Any?>, docEntryOriginal: Int): Int? {
        return (pagamento["PaymentInvoices"] as? List<*>)?.firstOrNull()?.let { invoice ->
            (invoice as? Map<*, *>)?.get("DocEntry") as? Int
        } ?: run {
            logger.warning("Não foi possível obter o DocEntry da nota fiscal em 'PaymentInvoices' para docEntry $docEntryOriginal")
            null
        }
    }
}