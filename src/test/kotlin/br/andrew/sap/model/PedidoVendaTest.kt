package br.andrew.sap.model

import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.pricing.ComissaoService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class PedidoVendaTest {


    @Test
    fun fazSemSplitFormaPgamento(){
        val pedido = PedidoVenda("cli","2020-01-01", "-1", "obs",
            listOf(), "1", )
        val order = pedido.getOrder(mock(ItemsService::class.java), mock(ComissaoService::class.java))
        Assertions.assertEquals("-1",pedido.idFormaPagamento)
        Assertions.assertEquals("-1",order.paymentMethod)
    }


    @Test
    fun fazSplitFormaPgamento(){
        val pedido = PedidoVenda("cli","2020-01-01", "forma", "5_-1",
            listOf(), "1", )
        val order = pedido.getOrder(mock(ItemsService::class.java), mock(ComissaoService::class.java))
        Assertions.assertEquals("5_-1",pedido.idCondicaoPagamento)
        Assertions.assertEquals("-1",order.paymentGroupCode)
    }


}