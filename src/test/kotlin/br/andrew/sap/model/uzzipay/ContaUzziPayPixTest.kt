package br.andrew.sap.model.uzzipay

import br.andrew.sap.model.bank.Payment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ContaUzziPayPixTest {

    @Test
    fun contaTransitoriaDefineContaForPayment() {
        val conta = ContaUzziPayPix().also {
            it.contaContabilBanco = "1.01.01"
            it.transitoria = "1.01.99"
        }

        assertEquals("1.01.99", conta.getAccountForPayment())
        assertEquals("1.01.99", conta.contaForPayment)
    }

    @Test
    fun pagamentoUsaContaTransitoriaQuandoConfigurada() {
        val conta = ContaUzziPayPix().also {
            it.contaContabilBanco = "1.01.01"
            it.transitoria = "1.01.99"
        }
        val transaction = Transaction("TX-1").also {
            it.paymentDate = "2026-02-09"
            it.receivedAmount = 10.0
        }

        val payment = Payment(transaction, conta)

        assertEquals("1.01.99", payment.cashAccount)
    }

    @Test
    fun pagamentoSemContaTransitoriaUsaContaBanco() {
        val conta = ContaUzziPayPix().also {
            it.contaContabilBanco = "1.01.01"
        }
        val transaction = Transaction("TX-2").also {
            it.paymentDate = "2026-02-09"
            it.receivedAmount = 10.0
        }

        val payment = Payment(transaction, conta)

        assertEquals("1.01.01", payment.cashAccount)
    }

    @Test
    fun hasTransitoriaSemFilialTransitoriaLancaErro() {
        val conta = ContaUzziPayPix().also {
            it.contaContabilBanco = "1.01.01"
            it.transitoria = "1.01.99"
        }

        val erro = assertThrows<Exception> { conta.hasTransitoryAccount() }
        assertTrue((erro.message ?: "").contains("idFilialTransitoria"))
    }

    @Test
    fun hasTransitoriaComFilialTransitoriaRetornaTrue() {
        val conta = ContaUzziPayPix().also {
            it.contaContabilBanco = "1.01.01"
            it.transitoria = "1.01.99"
            it.idFilialTransitoria = "2"
        }

        assertEquals(true, conta.hasTransitoryAccount())
    }
}
