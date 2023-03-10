package br.andrew.sap.rovema.model


import br.andrew.sap.rovema.infrastructure.odata.Order
import br.andrew.sap.rovema.infrastructure.odata.OrderBy
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OrderByTest {

    @Test
    fun test(){
        val saida = OrderBy(mapOf("windson" to Order.DESC))
        Assertions.assertEquals("\$orderby=windson desc",saida.toString())
    }

    @Test
    fun orderVazio(){
        val saida = OrderBy()
        Assertions.assertEquals("",saida.toString())
    }
}