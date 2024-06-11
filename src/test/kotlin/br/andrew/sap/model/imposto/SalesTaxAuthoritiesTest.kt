package br.andrew.sap.model.imposto

import br.andrew.sap.model.SalesTaxAuthorities
import br.andrew.sap.model.documents.base.AdditionalExpenses
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.base.Product
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SalesTaxAuthoritiesTest {

    @Test
    fun testDesonerado(){
        val produtos = listOf(
            (Product("PAC0000069","1","121.2122",5).also {
                it.valorDesonerado = BigDecimal("21.21")
            })
        )

        val order = OrderSales("",null,produtos,"")

        val resultado = SalesTaxAuthorities(1,17.5,100.0,100.0,100.0).valorImposto(produtos[0])

        order.totalDiscount = "5.0"
        order.aplicaDescontoDesonerado()
        Assertions.assertEquals(121.21,order.total())
        Assertions.assertEquals(BigDecimal("21.21"),resultado)
        Assertions.assertEquals(17.4986,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }


    @Test
    fun testImposto30Itens(){
        val produtos = listOf(
            (Product("PAC0000069","30","121.2122",5).also {
                it.valorDesonerado = BigDecimal("636.37")
            })
        )
        val order = OrderSales("",null,produtos,"")
        Assertions.assertEquals(3636.37,order.total())


        val resultado = SalesTaxAuthorities(1,17.5,100.0,100.0,100.0).valorImposto(produtos[0])
        Assertions.assertEquals(BigDecimal("636.36"),resultado)

        order.totalDiscount = "5.0"
        order.aplicaDescontoDesonerado()

        Assertions.assertEquals(17.5001,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }


    @Test
    fun testImposto30ItensDespesaAdicional(){
        val produtos = listOf(
            (Product("PAC0000069","30","121.2122",5).also {
                it.valorDesonerado = BigDecimal("636.37")
            })
        )
        val order = OrderSales("",null,produtos,"")
        order.documentAdditionalExpenses = mutableListOf(AdditionalExpenses(1,100.0))

        Assertions.assertEquals(3736.37,order.total())

        val resultado = SalesTaxAuthorities(1,17.5,100.0,100.0,100.0).valorImposto(produtos[0])
        Assertions.assertEquals(BigDecimal("636.36"),resultado)
        order.aplicaDescontoDesonerado()
        Assertions.assertEquals(17.0318,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }

    @Test
    fun testImpostoDespesaAdicional(){
        val produtos = listOf(
            (Product("PAC0000069","30","121.2122",5).also {
                it.valorDesonerado = BigDecimal("636.37")
            })
        )
        val order = OrderSales("",null,produtos,"")
        order.documentAdditionalExpenses = mutableListOf(AdditionalExpenses(1,500.0))

        Assertions.assertEquals(4136.37,order.total())

        val resultado = SalesTaxAuthorities(1,17.5,100.0,100.0,100.0).valorImposto(produtos[0])
        Assertions.assertEquals(BigDecimal("636.36"),resultado)
        order.aplicaDescontoDesonerado()
        Assertions.assertEquals(15.3847,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }

    @Test
    fun testDoisItensImposto(){
        val produtos = listOf(
            Product("PAC0000069","30","121.2122",5).also {
                it.valorDesonerado = BigDecimal("636.36")
            },
            Product("PAC0000099","1","242.4243",5).also {
                it.valorDesonerado = BigDecimal("42.42")
            },
        )

        val order = OrderSales("",null,produtos,"")

        Assertions.assertEquals(3878.79,order.total())
        val resultado = produtos.sumOf { SalesTaxAuthorities(1,17.5,100.0,100.0,100.0).valorImposto(it)}
        Assertions.assertEquals(BigDecimal("678.78"),resultado)
        order.aplicaDescontoDesonerado()
        Assertions.assertEquals(17.4998,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }

    @Test
    fun testDoisItensImpostoDespesaAdicional(){
        val produtos = listOf(
            Product("PAC0000069","30","121.2122",5).also {
                it.valorDesonerado = BigDecimal("636.37")
            },
            Product("PAC0000099","1","242.4243",5).also {
                it.valorDesonerado = BigDecimal("42.42")
            },
        )

        val order = OrderSales("",null,produtos,"")
        order.documentAdditionalExpenses = mutableListOf(AdditionalExpenses(1,500.0))

        Assertions.assertEquals(4378.79,order.total())

        val resultado = produtos.sumOf { SalesTaxAuthorities(1,17.5,100.0,100.0,100.0).valorImposto(it)}
        Assertions.assertEquals(BigDecimal("678.78"),resultado)
        order.aplicaDescontoDesonerado()
        Assertions.assertEquals(15.5018,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }

    @Test
    fun testeDoisItensComDesconto(){
        val produtos = listOf(
            Product("PAC0000069","30","134.6803",5).also {
                it.valorDesonerado = BigDecimal("636.36")
                it.DiscountPercent = 10.0
            },
            Product("PAC0000099","1","269.3604",5).also {
                it.valorDesonerado = BigDecimal("42.42")
                it.DiscountPercent = 10.0
            },
        )

        val order = OrderSales("",null,produtos,"")
        order.documentAdditionalExpenses = mutableListOf(AdditionalExpenses(1,500.0))

        Assertions.assertEquals(4378.79,order.total())

        val resultado = produtos.sumOf { SalesTaxAuthorities(1,17.5,100.0,100.0,100.0).valorImposto(it)}
        Assertions.assertEquals(BigDecimal("678.78"),resultado)
        order.aplicaDescontoDesonerado()
        Assertions.assertEquals(15.5015,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }
}