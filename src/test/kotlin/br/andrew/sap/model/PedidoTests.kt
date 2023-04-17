package br.andrew.sap.model

import br.andrew.sap.model.sovis.PedidoVenda
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PedidoTests {

    @Test
    fun pedidoToDocumentTest(){
        val pedido = PedidoVenda("windson","empWindson","formaPagamento",
                "condicao", listOf()).also { it.idPedido = "666" }
        val documento = pedido.getOrder()
        Assertions.assertEquals("666",documento.u_id_pedido_forca)
    }
}