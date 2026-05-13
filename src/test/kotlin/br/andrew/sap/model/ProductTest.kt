package br.andrew.sap.model

import br.andrew.sap.model.sap.documents.base.Product
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ProductTest {

    @Test
    fun presumeDesoneradoTest(){
        val produto = Product("10","10","145")
        Assertions.assertEquals(253.75,produto.presumeDesonerado(17.5))
        Assertions.assertEquals((BigDecimal("1450.00")),produto.total().setScale(2))
        Assertions.assertEquals(1230.00+11.25-45,produto.total().toDouble()-produto.presumeDesonerado(17.5))
        Assertions.assertEquals("10",produto.ItemCode)
    }

    @Test
    fun produtoDeveTerUsageNullPadrao(){
        val produto = Product("10","10","145")
        Assertions.assertEquals(null,produto.Usage)
    }
}