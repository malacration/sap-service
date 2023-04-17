package br.andrew.sap.model

import br.andrew.sap.model.sovis.Produto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ProdutoTests {

    @Test
    fun produtoToDocumentLine(){
        val produto = Produto("windson",100.0,1.0)
                .also {
                    it.desconto = 10.0
                    it.precoUnitario = 90.0
                }
        val itemSap = produto.getProduct(5)
        val valorNegociado = 90.0
        val valorUnitario = 100.0
        Assertions.assertEquals(valorNegociado, itemSap.U_preco_negociado)
        Assertions.assertEquals(0.00, itemSap.u_preco_base)
        Assertions.assertEquals(10.0, itemSap.discountPercent)
        Assertions.assertEquals(valorUnitario.toString(), itemSap.unitPrice)
    }
}