package br.andrew.sap.services.document

import JournalEntry
import JournalEntryLines
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.model.bank.PaymentInvoice
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AccountsReceivableService(
    private val incomingPaymentService: IncomingPaymentService,
    private val batchService: BatchService,
    private val journalEntriesService: JournalEntriesService,
    private val bankPlusService: BankPlusService
) {
    private val logger: Logger = LoggerFactory.getLogger(AccountsReceivableService::class.java)

    fun baixaPixBy(document: Document, installment: Installment, transaction: Transaction, conta: ContaUzziPayPix): Transaction {
        if(!transaction.paid) {
            throw Exception("O Pagamento não foi realizado ainda")
        }
        val payment = Payment(transaction, conta).also {
            it.cardCode = document.CardCode
            it.setBPID(document.getBPL_IDAssignedToInvoice())
            it.paymentInvoices = listOf(
                PaymentInvoice(document, transaction, installment)
            )
        }
        val batchList = BatchList()
        batchList.add(BatchMethod.POST, payment, incomingPaymentService)

        if (conta.hasTransitoryAccount()) {
            val filialTransitoria = conta.idFilialTransitoria
                ?.toIntOrNull() ?: throw Exception("Lancamento sem filial transitoria")
            val valor = transaction.receivedAmount ?: throw Exception("Transacao sem valor recebido")
            val deb = JournalEntryLines(conta.contaContabilBanco, valor, 0.0, filialTransitoria)
            val cred = JournalEntryLines(conta.transitoria!!, 0.0, valor, filialTransitoria)
            val entry = JournalEntry(
                listOf(deb, cred),
                "Transferencia Pix transitoria ${transaction.txId} - ${memoSuffix(document)}"
            ).also {
                it.Reference = transaction.txId
            }
            batchList.add(BatchMethod.POST, entry, journalEntriesService)
        }
        batchService.run(batchList)
        cancelBoletos(document, installment)
        return transaction
    }

    private fun memoSuffix(document: Document): String {
        return when(document) {
            is Invoice -> "INV ${document.docNum}"
            is DownPayment -> "ADI ${document.docNum}"
            else -> "DOC ${document.docNum}"
        }
    }

    private fun cancelBoletos(document: Document, installment: Installment) {
        if(document !is Invoice) {
            return
        }
        val boletos = try {
            bankPlusService.getBoletosBy(document)
        } catch (e: Exception) {
            logger.warn(e.message, e)
            null
        }
        boletos
            ?.filter { installment.getBy(it) }
            ?.forEach { bankPlusService.cancelarBoleto(it) }
    }
}
