package br.andrew.sap.model.documents

import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.futura.ItemRetirada
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OrderSalesTests {


    @Test
    fun getQuotationVendaFuturaTest(){
        val produtos = listOf(Product("item","100","35.00"))
        val order = OrderSales("","",produtos,"2").also {
            it.frete = 100.00
        }
        Assertions.assertEquals(100.00,order.totalDespesaAdicional().toDouble())
        val retirada = PedidoRetirada(50, listOf(ItemRetirada("item",50.00)))
        val resultado = order.getQuotationVendaFutura(retirada,"",5)
        Assertions.assertEquals(50.00,resultado.totalDespesaAdicional().toDouble())
    }

    @Test
    fun testFreteProporcionalMetadeQuantidade() {
        val produtos = listOf(Product("item1", "100", "50.00"))
        val order = OrderSales("", "", produtos, "4").also {
            it.frete = 75.00  // Frete inicial
        }
        // Testa o total da despesa inicial (deve ser igual ao frete do pedido de venda)
        Assertions.assertEquals(75.00, order.totalDespesaAdicional().toDouble(), "Falha ao verificar o frete inicial")

        // Pedido de retirada proporcional (metade da quantidade)
        val retirada = PedidoRetirada(50, listOf(ItemRetirada("item1", 50.00)))
        val resultado = order.getQuotationVendaFutura(retirada, "", 2)

        // O frete proporcional deve ser a metade do frete original (75 / 2 = 37.50)
        Assertions.assertEquals(37.50, resultado.totalDespesaAdicional().toDouble(), "Falha ao verificar o frete proporcional")
    }

    @Test
    fun testFreteProporcionalComArredondamento() {
        val produtos = listOf(Product("item2", "200", "15.75"))
        val order = OrderSales("", "", produtos, "5").also {
            it.frete = 123.45  // Frete que pode gerar arredondamento
        }
        // Testa o frete inicial
        Assertions.assertEquals(123.45, order.totalDespesaAdicional().toDouble(), "Falha ao verificar o frete inicial com valor fracionado")

        // Pedido de retirada que retira 60 de 200 itens (30%)
        val retirada = PedidoRetirada(60, listOf(ItemRetirada("item2", 60.00)))
        val resultado = order.getQuotationVendaFutura(retirada, "", 3)
        // Frete proporcional (123.45 * (60/200) = 37.035)
        Assertions.assertEquals(37.03, resultado.totalDespesaAdicional().toDouble(), "Falha ao verificar o frete com arredondamento")

        val retirada2 = PedidoRetirada(60, listOf(ItemRetirada("item2", 140.0)))
        val resultado2 = order.getQuotationVendaFutura(retirada2, "", 3)
        Assertions.assertEquals(86.41, resultado2.totalDespesaAdicional().toDouble(), "Falha ao verificar o frete com arredondamento")


        Assertions.assertEquals(123.44, resultado2.totalDespesaAdicional().plus(resultado.totalDespesaAdicional()).toDouble(), "Falha ao verificar o frete com arredondamento")


    }
}