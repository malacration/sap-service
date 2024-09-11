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
}