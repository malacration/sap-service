package br.andrew.sap.services

import br.andrew.sap.model.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.SalesTaxAuthorities
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import br.andrew.sap.model.forca.PedidoVenda
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class OrderSalesServiceTest {

    @Test
    fun test(){
        val json = "{\"dataEntraga\":\"2023-04-28\",\"observacao\":\"TESTE\",\"idCliente\":\"CLI0003017\",\"desconto\":0,\"produtos\":{\"precoUnitario\":108.9,\"idProduto\":\"PAC0000042\",\"desconto\":10,\"valorTabela\":121,\"quantidade\":5},\"idCondicaoPagamento\":16,\"frete\":10,\"idEmpresa\":2,\"tipoPedido\":9,\"codVendedor\":58,\"idPedido\":106,\"idFormaPagamento\":\"AVISTA\"}"
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val pedidoBase = mapper.readValue(json, jacksonTypeRef<PedidoVenda>())
        val orderBase = pedidoBase.getOrder()

        val negociadoEsperado = 544.50

        Assertions.assertEquals("106",orderBase.u_id_pedido_forca)
        Assertions.assertEquals(554.5,orderBase.total())
        Assertions.assertEquals(negociadoEsperado,orderBase.totalNegociado())
        Assertions.assertEquals(10.0,orderBase.totalDespesaAdicional())
        Assertions.assertEquals(0.0,orderBase.discountPercent)
        orderBase.DocumentLines.forEach {  Assertions.assertEquals(10.0,it.DiscountPercent) }
        orderBase.DocumentLines.forEach {  Assertions.assertEquals("121.0",it.UnitPrice) }


        orderBase.DocumentLines.forEach {
            it.UnitPrice = PrecoUnitarioComDesoneracao()
                    .calculaPreco(it, SalesTaxAuthorities(-1,17.5,0.0,0.0,100.0)).toString()
        }
        val totalDesonerado = orderBase.total()-orderBase.presumeDesonerado(17.5)

        Assertions.assertEquals(670.0006,orderBase.total())
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
}