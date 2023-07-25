package br.andrew.sap.model

import br.andrew.sap.model.forca.Produto
import br.andrew.sap.services.ComissaoServiceMock
import br.andrew.sap.services.ItemServiceMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ProdutoTests {

    @Test
    fun produtoToDocumentLine(){
        val produto = Produto("windson",100.0,1.0,5)
                .also {
                    it.desconto = 10.0
                    it.precoUnitario = 90.0
                }
        val itemSap = produto.getProduct(5,
            ItemServiceMock.get(),
            ComissaoServiceMock.get()
        )
        val valorNegociado = 90.0
        val valorUnitario = 100.0
        Assertions.assertEquals(valorNegociado, itemSap.U_preco_negociado)
        Assertions.assertEquals(66.6, itemSap.U_preco_base)
        Assertions.assertEquals(10.0, itemSap.DiscountPercent)
        Assertions.assertEquals(valorUnitario.toString(), itemSap.UnitPrice)
    }
}