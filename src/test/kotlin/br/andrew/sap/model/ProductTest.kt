package br.andrew.sap.model

import br.andrew.sap.model.documents.Product
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ProductTest {

    @Test
    fun presumeDesoneradoTest(){
        val produto = Product("10","10","145")
        Assertions.assertEquals(253.75,produto.presumeDesonerado(17.5))
        Assertions.assertEquals(1450.0,produto.total())
        Assertions.assertEquals(1230.00+11.25-45,produto.total()-produto.presumeDesonerado(17.5))
    }
}