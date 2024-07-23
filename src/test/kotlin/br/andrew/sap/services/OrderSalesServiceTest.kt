package br.andrew.sap.services

import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.forca.PedidoVenda
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.math.BigDecimal


class OrderSalesServiceTest {

    @Test
    fun test(){
        val json = "{\"dataEntraga\":\"2023-04-28\",\"observacao\":\"TESTE\",\"idCliente\":\"CLI0003017\",\"desconto\":0,\"produtos\":{\"precoUnitario\":108.9,\"idProduto\":\"PAC0000042\",\"desconto\":10,\"valorTabela\":121,\"quantidade\":5},\"idCondicaoPagamento\":16,\"frete\":10,\"idEmpresa\":2,\"tipoPedido\":9,\"codVendedor\":58,\"idPedido\":106,\"idFormaPagamento\":\"AVISTA\"}"
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val pedidoBase = mapper.readValue(json, jacksonTypeRef<PedidoVenda>()).also { it.produtos.forEach { it.listapreco = 5 } }
        val orderBase = pedidoBase.getOrder(
            Mockito.mock(ItemsService::class.java),
            ComissaoServiceMock.get()
        )

        val negociadoEsperado = 544.50

        Assertions.assertEquals("106",orderBase.u_id_pedido_forca)
        Assertions.assertEquals(554.5,orderBase.total())
        Assertions.assertEquals(negociadoEsperado,orderBase.totalNegociado())
        Assertions.assertEquals(BigDecimal("10"),orderBase.totalDespesaAdicional())
        Assertions.assertEquals(0.0,orderBase.discountPercent)
        orderBase.DocumentLines.forEach {  Assertions.assertEquals(10.0,it.DiscountPercent) }
        orderBase.DocumentLines.forEach {  Assertions.assertEquals("121.0",it.UnitPrice) }


        orderBase.DocumentLines.forEach {
            it.UnitPrice = PrecoUnitarioComDesoneracao()
                    .calculaPreco(it, SalesTaxAuthorities(-1,17.5,0.0,0.0,100.0)).toString()
        }
        orderBase.total()-orderBase.presumeDesonerado(17.5)

        Assertions.assertEquals(670.0,orderBase.total())
        Assertions.assertEquals(544.50,orderBase.totalNegociado())
    }

    @Test
    fun totalComDesconto(){
        val produtos = listOf<Product>(
            (Product("PAC0000069","1","100",5).also { it.DiscountPercent = 10.0 })
        )
        val order = OrderSales("",null,produtos,"")
        Assertions.assertEquals(90.0,order.total())
    }

    @Test
    fun totalSemDescontgo(){
        val produtos = listOf<Product>(
            (Product("PAC0000069","1","100",5).also {
                it.valorDesonerado = BigDecimal("10")
            })
        )

        val order = OrderSales("",null,produtos,"")
        order.totalDiscount = "5.0"
        order.aplicaDescontoDesonerado()
        Assertions.assertEquals(100.0,order.total())
        Assertions.assertEquals(10.0,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }

    @Test
    fun totalSemDesoneradoValorMaior(){
        val produtos = listOf<Product>(
            (Product("PAC0000069","1","121.2122",5).also {
                it.valorDesonerado = BigDecimal("21.21")
            })
        )

        val order = OrderSales("",null,produtos,"")
        order.totalDiscount = "5.0"
        order.aplicaDescontoDesonerado()
        Assertions.assertEquals(121.21,order.total())
        Assertions.assertEquals(17.4986,order.discountPercent)
        Assertions.assertEquals(null,order.totalDiscount)
    }
}