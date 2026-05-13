package br.andrew.sap.services.uzzipay

import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.document.AccountsReceivableService
import br.andrew.sap.services.document.InvoiceService
import org.springframework.stereotype.Service

@Service
class PixPaymentVerificationService(
    private val transactionsPixService: TransactionsPixService,
    private val invoiceService: InvoiceService,
    private val accountsReceivableService: AccountsReceivableService
) {

    fun verificaPixEhBaixa(document: Document, installment: Installment): Transaction {
        val conta = transactionsPixService.getContaBy(document)
        val reference = installment.U_pix_reference ?: throw Exception("Referencia a Parcela não encontrada")
        val transaction = transactionsPixService.getBy(reference, conta)
        if(transaction.paid) {
            return accountsReceivableService.baixaPixBy(document, installment, transaction, conta)
        }
        return transaction
    }

    fun verificaPixEhBaixa(reference: String, conta: ContaUzziPayPix): Transaction {
        val transaction = transactionsPixService.getBy(reference, conta)
        if(transaction.paid) {
            return invoiceService.baixaPixBy(transaction, conta)
        }
        return transaction
    }
}
