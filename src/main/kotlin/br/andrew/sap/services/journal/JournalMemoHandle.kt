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



    fun atualizarGrupoEconomicoECentroCusto(lancamento: JournalEntry) {
        var journalId = lancamento.JdtNum ?: throw Exception("O id de JournalEntry nao pode ser nulo, esse registro")
        val docEntryOriginal = lancamento.Original
            ?: throw Exception("Campo 'Original' está nulo para o JournalEntry $journalId")

        when (lancamento.OriginalJournal) {
            "ttVendorPayment" -> {
                val pagamento = vendorPaymentService.getById(docEntryOriginal)
                val docEntryNota = extrairDocEntryNota(pagamento)
                    ?: throw Exception("Não foi possível obter o DocEntry da nota fiscal em 'PaymentInvoices'. no $docEntryOriginal")

                val grupo = purchaseInvoiceService.getCostingCode(docEntryNota, "CostingCode")
                    ?: throw Exception("Grupo Econômico não encontrado na nota fiscal $docEntryNota")
                val centro = purchaseInvoiceService.getCostingCode(docEntryNota, "CostingCode2")
                    ?: throw Exception("Centro de Custo não encontrado na nota fiscal $docEntryNota")

                journalEntryService.updateGrupoEconomicoJournalEntry(journalId, grupo, centro)
            }

            "ttReceipt" -> {
                val pagamento = incomingPaymentService.getById(docEntryOriginal)
                val docEntryNota = extrairDocEntryNota(pagamento)
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

    //TODO testar o metodo em homologacao e verificar se funciona adequadamente
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

    @Deprecated("Se o metodo atribuiCentroCustoEmContasRecebeOuPagar funcionar remover esse")
    private fun extrairDocEntryNota(pagamento: Map<String, Any?>): Int? {
        return (pagamento["PaymentInvoices"] as? List<*>)?.firstOrNull()?.let { invoice ->
            (invoice as? Map<*, *>)?.get("DocEntry") as? Int
        }
    }
}