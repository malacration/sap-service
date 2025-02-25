package br.andrew.sap.model

import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import br.andrew.sap.model.self.vendafutura.ItemTroca
import br.andrew.sap.model.self.vendafutura.PedidoTroca
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.pricing.ComissaoService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito

class ContratoTest {

    val item= Item(
        "a",
        "",
        10.0,
        100.0,
        10.0,
        0.0,
        10.0,
        "")

    val itemServiceMock : ItemsService
    val comissaoServiceMock : ComissaoService

    init {
        itemServiceMock = Mockito.mock(ItemsService::class.java)
        //Mockito.`when`(itemServiceMock.(Mockito.anyString(), Mockito.anyInt())).thenReturn(66.6)

        comissaoServiceMock = Mockito.mock(ComissaoService::class.java)
        Mockito.`when`(comissaoServiceMock.getByIdTabela(Mockito.anyInt())).thenReturn(Comissao(1,5.0))
    }

    @Test
    fun trocaTotal(){
        val itens = mutableListOf<Item>(item)
        val contrato = Contrato(1,"",itens,666,"",2,0.0)
        val itemTroca = listOf(ItemTroca(
            "a",
            100.0,
            10.0
        ))
        val itemRecebido = listOf(
            Product("b","10.0","10.0",0).also {
                it.ItemDescription = "Descricao"
                it.MeasureUnit = "un"
                it.ItemDescription = "Descricao"
                it.MeasureUnit = "un"
                it.PriceList = 1
                it.U_preco_negociado = 10.0
                it.DiscountPercent = 0.0
            }
        )

        val pedridoTroca = PedidoTroca("1",itemTroca,itemRecebido)
        val balanco = contrato.troca(pedridoTroca,itemServiceMock,comissaoServiceMock)
        Assertions.assertEquals(1,contrato.itens.size)
        Assertions.assertEquals("b",contrato.itens.first().U_itemCode)

        Assertions.assertEquals(-900.0,balanco.toDouble())
    }

    @Test
    fun trocaParcial(){
        val itens = mutableListOf<Item>(item)
        val contrato = Contrato(1,"",itens,666,"",2,0.0)
        val itemTroca = listOf(ItemTroca(
            "a",
            50.0,
            10.0
        ))

        val itemRecebido = listOf(
            Product("b","100.0","100.0",0).also {
                it.ItemDescription = "Descricao"
                it.MeasureUnit = "un"
                it.PriceList = 1
                it.U_preco_negociado = 10.0
                it.DiscountPercent = 0.0
            }
        )

        val pedridoTroca = PedidoTroca("1",itemTroca,itemRecebido)
        val balanco = contrato.troca(pedridoTroca,itemServiceMock,comissaoServiceMock)
        Assertions.assertEquals(2,contrato.itens.size)
        Assertions.assertEquals("a",contrato.itens.first().U_itemCode)
        Assertions.assertEquals(50.0,contrato.itens.first().U_quantity)
        Assertions.assertEquals("b",contrato.itens.get(1).U_itemCode)
        Assertions.assertEquals(100.0,contrato.itens.get(1).U_quantity)
        Assertions.assertEquals(500.0,balanco.toDouble())
    }


    @Test
    fun trocaParcialSemAdicionarItens(){
        val itens = mutableListOf<Item>(item)
        val contrato = Contrato(1,"",itens,666,"",2,0.0)
        val itemTroca = listOf(ItemTroca(
            "a",
            50.0,
            10.0
        ))

        val pedridoTroca = PedidoTroca("1",itemTroca,listOf())
        val balanco = contrato.troca(pedridoTroca,itemServiceMock,comissaoServiceMock)
        Assertions.assertEquals(1,contrato.itens.size)
        Assertions.assertEquals("a",contrato.itens.first().U_itemCode)
        Assertions.assertEquals(50.0,contrato.itens.first().U_quantity)
        Assertions.assertEquals(-500.0,balanco.toDouble())
    }


    @Test
    fun erroAoTrocarMaisItens(){
        val itens = mutableListOf<Item>(item)
        val contrato = Contrato(1,"",itens,666,"",2,0.0)
        val itemTroca = listOf(ItemTroca(
            "a",
            500.0,
            10.0
        ))
        val itemRecebido = listOf(
            Product("b","10.0","10.0",0)
        )

        val pedridoTroca = PedidoTroca("1",itemTroca,itemRecebido)
        val exception =  assertThrows<Throwable> {
            contrato.troca(pedridoTroca,itemServiceMock,comissaoServiceMock)
        }
        Assertions.assertEquals("Nao e possivel trocar uma quantidade superior ao contrato", exception.message)
    }
}