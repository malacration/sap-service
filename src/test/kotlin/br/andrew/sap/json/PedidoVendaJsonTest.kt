package br.andrew.sap.json

import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.model.forca.Produto
import br.andrew.sap.services.ComissaoServiceMock
import br.andrew.sap.services.stock.ItemsService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class PedidoVendaJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val produtos = listOf<Produto>(Produto("PRO0001",10.5,10.0,0))
        val pedido = PedidoVenda("CLI0001","18",
                "avista",
                "15", produtos,"5",2)
        println(mapper.writeValueAsString(pedido))
    }

    @Test
    fun testeJsonCliente(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        mapper.readValue(json, jacksonTypeRef<PedidoVenda>())
    }

    @Test
    fun testeJson2Cliente(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        mapper.readValue(json2, jacksonTypeRef<PedidoVenda>())
    }

    @Test
    fun testeJsonSingleToList(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        mapper.readValue(json2, jacksonTypeRef<PedidoVenda>())
    }

    @Test
    fun jsonExtend(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val produtos = listOf<Produto>(Produto("PRO0001",10.5,10.0,0)
                .also { it.desconto = 100.0 })
        val pedido = PedidoVenda("CLI0001","18",
                "avista",
                "15", produtos,"5",2)
                .also {
                    it.idPedido = "10"
                    it.desconto = 10.0
                }
        println(mapper.writeValueAsString(pedido))
    }

    @Test
    fun jsonPrecoUnitarioTabela(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(jsonPrecos, jacksonTypeRef<PedidoVenda>())
        val produto = obj.produtos.get(0)
        Assertions.assertEquals(140.0,produto.precoUnitario)
        Assertions.assertEquals(141.5,produto.valorTabela)
    }

    @Test
    fun jsonParseDataNull(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(jsonDataNull, jacksonTypeRef<PedidoVenda>())
    }

    @Test
    fun jsonParIdItem(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(jsonIdItem, jacksonTypeRef<PedidoVenda>())
        Assertions.assertEquals("898",obj.produtos.get(0).idItem)
    }

    @Test
    fun json77UnitPrice() {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json77, jacksonTypeRef<PedidoVenda>())
        Assertions.assertEquals(131.67,obj.produtos.get(0).precoUnitario)
        val order = obj.getOrder(Mockito.mock(ItemsService::class.java), ComissaoServiceMock.get())
        Assertions.assertEquals("138.6",order.DocumentLines.get(0).UnitPrice)
        Assertions.assertEquals(5.0,order.DocumentLines.get(0).DiscountPercent)
        Assertions.assertEquals(19,order.paymentGroupCode)
    }

    val jsonDataNull = "{\"dataEntraga\":null,\"observacao\":\"RETIRAR NA FABRICA\\nFRETE A R\$12,00/SC\",\"idCliente\":\"CLI0001475\",\"desconto\":0,\"produtos\":{\"precoUnitario\":113.19,\"idProduto\":\"PAC0000121\",\"desconto\":0,\"valorTabela\":113.19,\"quantidade\":30},\"idCondicaoPagamento\":20,\"frete\":360,\"idEmpresa\":2,\"tipoPedido\":9,\"codVendedor\":65,\"idPedido\":52,\"idFormaPagamento\":\"BB-RC-BOL-1199\"}"

    val json = "{\n" +
            "   \"idCliente\":\"\",\n" +
            "   \"idEmpresa\":\"2\",\n" +
            "   \"idFormaPagamento\":\"avista\",\n" +
            "   \"idCondicaoPagamento\":\"15\",\n" +
            "   \"idPedido\":\"15\",\n" +
            "   \"produtos\":[\n" +
            "      {\n" +
            "         \"idProduto\":\"PAC0000105\",\n" +
            "         \"precoUnitario\":143.0141,\n" +
            "         \"quantidade\":1\n" +
            "      }\n" +
            "   ],\n" +
            "   \"codVendedor\":\"54\"\n" +
            "}"
    val json2 = "{\n" +
            "   \"idCliente\": \"CLI0002776\",\n" +
            "   \"idPedido\": \"CLI0002776\",\n" +
            "   \"produtos\": [\n" +
            "      {\n" +
            "         \"precoUnitario\": 157.8008,\n" +
            "         \"idProduto\": \"PAC0000105\",\n" +
            "         \"quantidade\": 1\n" +
            "      },\n" +
            "      {\n" +
            "         \"precoUnitario\": 155.5704,\n" +
            "         \"idProduto\": \"PAC0000118\",\n" +
            "         \"quantidade\": 4\n" +
            "      }\n" +
            "   ],\n" +
            "   \"idCondicaoPagamento\": 15,\n" +
            "   \"idEmpresa\": 2,\n" +
            "   \"codVendedor\": 54,\n" +
            "   \"idFormaPagamento\": \"AVISTA\"\n" +
            "}"

    val jsonSingleProduto = "{\"idCliente\":\"CLI0002777\",\"produtos\":{\"precoUnitario\":162.3362,\"idproduto\":\"PAC0000118\",\"quantidade\":4},\"idCondicaoPagamento\":\"\",\"idEmpresa\":2,\"idFormaPagamento\":\"AVISTA\"}"

    val jsonPrecos = "{\"dataEntraga\":\"2023-04-17\",\"observacao\":\"\",\"idCliente\":\"CLI0002773\",\"desconto\":0,\"produtos\":{\"precoUnitario\":140.0,\"idProduto\":\"PAC0000105\",\"desconto\":0,\"valorTabela\":141.5,\"quantidade\":1},\"idCondicaoPagamento\":15,\"frete\":0,\"idEmpresa\":2,\"tipoPedido\":16,\"codVendedor\":54,\"idPedido\":49,\"idFormaPagamento\":\"BB-RC-BOL-1199\"}\n"

    val json77 = "{\"dataEntraga\":\"2023-08-01\",\"observacao\":\"TESTE\",\"idCliente\":\"CLI0002058\",\"desconto\":0,\"produtos\":{\"precoUnitario\":131.67,\"listapreco\":4,\"idProduto\":\"PAC0000118\",\"desconto\":5,\"valorTabela\":138.6,\"quantidade\":2,\"idItem\":106},\"idCondicaoPagamento\":\"1_19\",\"frete\":10,\"idEmpresa\":2,\"tipoPedido\":16,\"codVendedor\":38,\"idPedido\":73,\"idFormaPagamento\":\"BB-RC-BOL-1199\"}"
    val jsonIdItem = "{\"dataEntraga\":\"2023-06-02\",\"observacao\":\"DESCONTO 4% TABELA MAIS 15% REVENDA \\nFRETE R\$ 5,00 O SACO COMBINADO COM A GERENCIA \\nTRAZER CHAPAS DE COLORADO \\nENTREGA NA JABURU AGROPECUARIA\",\"idCliente\":\"CLI0000428\",\"desconto\":0,\"produtos\":{\"precoUnitario\":77.63,\"idProduto\":\"PAC0000100\",\"desconto\":9.1409176,\"valorTabela\":85.44,\"quantidade\":50,\"idItem\":898},\"idCondicaoPagamento\":2,\"frete\":0,\"idEmpresa\":2,\"tipoPedido\":16,\"codVendedor\":5,\"idPedido\":629,\"idFormaPagamento\":\"AVISTA\"}"
}
