package br.andrew.sap.model.documents

import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Product
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DocumentTest{

    @Test
    fun testeTotalToBigDecimal(){
        val produtos = listOf(Product("windson","100","1.7732",10).also { it.U_preco_negociado = 1.7732 })
        val document = Invoice("","",produtos,"2")
        Assertions.assertEquals("177.32",document.totalNegociado().toString())
    }
}