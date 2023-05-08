package br.andrew.sap.model

import br.andrew.sap.model.forca.PedidoVenda
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PedidoTests {

    @Test
    fun pedidoToDocumentTest(){
        val pedido = PedidoVenda("windson","empWindson","formaPagamento",
                "condicao", listOf(),"666")
        val documento = pedido.getOrder()
        Assertions.assertEquals("666",documento.u_id_pedido_forca)
        Assertions.assertEquals("1",documento.u_pedido_update)
    }
}