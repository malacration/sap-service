package br.andrew.sap.model

import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.OrderSales
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OrderSalesTest {

    fun agrupamentoImposto(){
        throw Exception("Arrumar teste de agrupamento de impostos")
    }

    fun orderSalesPrecoBase(){
        throw Exception("Arrumar teste de agrupamento de impostos")
    }

    @Test
    fun orderIsDocument(){
        Assertions.assertTrue(OrderSales("",null, listOf(),"") is Document)
    }


}