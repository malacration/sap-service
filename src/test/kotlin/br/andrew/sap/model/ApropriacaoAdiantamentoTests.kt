package br.andrew.sap.model

import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.adiantamento.ApropriacaoAdiantamento
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ApropriacaoAdiantamentoTests {


    @Test
    fun teste(){
        val adiantamento1 = DownPayment("","", listOf(),"").also {
            it.docEntry = 1
            it.docNum = "1"
            it.apropriado = BigDecimal.ZERO
            it.DocTotal = "100.00"
        }
        val adiantamentos : List<DownPayment> = listOf(
            adiantamento1
        )
        val invoice = Invoice("","", listOf(),"").also {
            it.DocTotal = "100"
        }
        val resultado = ApropriacaoAdiantamento(invoice,adiantamentos).get()

        Assertions.assertEquals(1,resultado.size)
        Assertions.assertEquals(1,resultado.get(0).docEntry)
    }

    @Test
    fun naoTemPropriacao(){
        val adiantamento1 = DownPayment("","", listOf(),"").also {
            it.docEntry = 1
            it.docNum = "1"
            it.apropriado = BigDecimal(100)
            it.DocTotal = "100.00"
        }
        val adiantamentos : List<DownPayment> = listOf(
            adiantamento1
        )
        val invoice = Invoice("","", listOf(),"").also {
            it.DocTotal = "120"
        }
        val resultado = ApropriacaoAdiantamento(invoice,adiantamentos).get()

        Assertions.assertEquals(0,resultado.size)
    }


    @Test
    fun `3 adiantamento, volta 2`(){
        val adiantamento1 = DownPayment("","", listOf(),"").also {
            it.docEntry = 1
            it.docNum = "1"
            it.apropriado = BigDecimal.ZERO
            it.DocTotal = "100.00"
        }
        val adiantamento2 = DownPayment("","", listOf(),"").also {
            it.docEntry = 1
            it.docNum = "1"
            it.apropriado = BigDecimal.ZERO
            it.DocTotal = "100.00"
        }
        val adiantamento3 = DownPayment("","", listOf(),"").also {
            it.docEntry = 1
            it.docNum = "1"
            it.apropriado = BigDecimal.ZERO
            it.DocTotal = "100.00"
        }

        val invoice = Invoice("","", listOf(),"")
        invoice.DocTotal = "150"
        val adiantamentos : List<DownPayment> = listOf(
            adiantamento1,adiantamento2,adiantamento3
        )
        val resultado = ApropriacaoAdiantamento(invoice,adiantamentos).get()

        Assertions.assertEquals(2,resultado.size)
    }
}