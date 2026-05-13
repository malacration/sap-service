package br.andrew.sap.services.document

import br.andrew.sap.model.exceptions.PixPaymentAmountExceededException
import br.andrew.sap.model.exceptions.PixPaymentDocumentClosedOrBlockedException
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class AccountsReceivableServiceTest {

    @Test
    fun baixaPixByTraduzErroQuandoValorInformadoPeloBancoEhMaiorQueValorDaInvoice() {
        val service = newService()
        val batchService = service.second
        val original = RuntimeException("1320000747 - Payment amount is greter than invoice amount")
        whenever(batchService.run(any<BatchList>())).thenThrow(original)

        val (conta, invoice, installment, transaction) = newScenario(bankAmount = 120.0)

        val erro = assertThrows<PixPaymentAmountExceededException> {
            service.first.baixaPixBy(invoice, installment, transaction, conta)
        }

        assertEquals(
            "O valor informado pelo banco é maior que o valor da invoice. Valor da invoice: 100,00. Valor informado pelo banco: 120,00.",
            erro.message
        )
        assertSame(original, erro.cause)
    }

    @Test
    fun baixaPixByIncluiTenteNovamenteQuandoValoresFormatadosForemIguais() {
        val service = newService()
        val batchService = service.second
        val original = RuntimeException("1320000747 - Payment amount is greter than invoice amount")
        whenever(batchService.run(any<BatchList>())).thenThrow(original)

        val (conta, invoice, installment, transaction) = newScenario(bankAmount = 100.004)

        val erro = assertThrows<PixPaymentAmountExceededException> {
            service.first.baixaPixBy(invoice, installment, transaction, conta)
        }

        assertEquals(
            "O valor informado pelo banco é maior que o valor da invoice. Valor da invoice: 100,00. Valor informado pelo banco: 100,00. Tente novamente.",
            erro.message
        )
        assertSame(original, erro.cause)
    }

    @Test
    fun baixaPixByTraduzErroQuandoDocumentoJaEstaFechadoOuBloqueado() {
        val service = newService()
        val batchService = service.second
        val original = RuntimeException("invoice is already closed or blocked")
        whenever(batchService.run(any<BatchList>())).thenThrow(original)

        val (conta, invoice, installment, transaction) = newScenario(bankAmount = 100.0)

        val erro = assertThrows<PixPaymentDocumentClosedOrBlockedException> {
            service.first.baixaPixBy(invoice, installment, transaction, conta)
        }

        assertEquals(
            "O Documento esta fechado ou cancelado, tente novamente.",
            erro.message
        )
        assertSame(original, erro.cause)
    }

    @Test
    fun baixaPixByMantemErroOriginalQuandoMensagemNaoEhDoValorMaiorQueInvoiceNemDocumentoFechado() {
        val service = newService()
        val batchService = service.second
        val original = RuntimeException("erro generico do SAP")
        whenever(batchService.run(any<BatchList>())).thenThrow(original)

        val (conta, invoice, installment, transaction) = newScenario(bankAmount = 120.0)

        val erro = assertThrows<Exception> {
            service.first.baixaPixBy(invoice, installment, transaction, conta)
        }

        assertSame(original, erro)
    }

    private fun newService(): Pair<AccountsReceivableService, BatchService> {
        val incomingPaymentService = mock(IncomingPaymentService::class.java)
        val batchService = mock(BatchService::class.java)
        val journalEntriesService = mock(JournalEntriesService::class.java)
        val bankPlusService = mock(BankPlusService::class.java)
        return AccountsReceivableService(
            incomingPaymentService,
            batchService,
            journalEntriesService,
            bankPlusService
        ) to batchService
    }

    private fun newScenario(bankAmount: Double): Quadruple<ContaUzziPayPix, Invoice, Installment, Transaction> {
        val conta = ContaUzziPayPix().also {
            it.contaContabilBanco = "1.1.1.01"
        }
        val invoice = Invoice("C0001", "2026-05-13", BPL_IDAssignedToInvoice = "1").also {
            it.docEntry = 10
            it.docObjectCode = DocumentTypes.oInvoices
        }
        val installment = Installment(null, 100.0).also {
            it.InstallmentId = 1
        }
        val transaction = Transaction("pix-123").also {
            it.paid = true
            it.receivedAmount = bankAmount
            it.paymentDate = "2026-05-13"
        }
        return Quadruple(conta, invoice, installment, transaction)
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
