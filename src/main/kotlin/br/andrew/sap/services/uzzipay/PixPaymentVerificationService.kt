package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.document.InvoiceService
import org.springframework.stereotype.Service

@Service
class PixPaymentVerificationService(
    private val uzziPayEnvrioment: UzziPayEnvrioment,
    private val transactionsPixService: TransactionsPixService,
    private val invoiceService: InvoiceService
) {

    fun verificaPixEhBaixa(invoice: Invoice, installment: Installment): Transaction {
        val conta = transactionsPixService.getContaBy(invoice)
        val reference = installment.U_pix_reference ?: throw Exception("Referencia a Parcela não encontrada")
        return verificaPixEhBaixa(reference, conta)
    }

    fun verificaPixEhBaixa(reference: String, conta: ContaUzziPayPix): Transaction {
        val transaction = transactionsPixService.getBy(reference, conta)
        if(transaction.paid) {
            return invoiceService.baixaPixBy(transaction, conta)
        }
        return transaction
    }
}
