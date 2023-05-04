package br.andrew.sap.json

import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.model.sovis.Produto
import br.andrew.sap.services.ItemsService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mock
import java.util.*

class PedidoVendaJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val produtos = listOf<Produto>(Produto("PRO0001",10.5,10.0))
        val pedido = PedidoVenda("CLI0001","18",
                "avista",
                "15", produtos,2)
        println(mapper.writeValueAsString(pedido))
    }

    @Test
    fun testeJsonCliente(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, jacksonTypeRef<PedidoVenda>())
    }

    @Test
    fun testeJson2Cliente(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json2, jacksonTypeRef<PedidoVenda>())
    }

    @Test
    fun testeJsonSingleToList(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json2, jacksonTypeRef<PedidoVenda>())
    }

    @Test
    fun jsonExtend(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val produtos = listOf<Produto>(Produto("PRO0001",10.5,10.0)
                .also { it.desconto = 100.0 })
        val pedido = PedidoVenda("CLI0001","18",
                "avista",
                "15", produtos,2)
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

    val jsonDataNull = "{\"dataEntraga\":null,\"observacao\":\"RETIRAR NA FABRICA\\nFRETE A R\$12,00/SC\",\"idCliente\":\"CLI0001475\",\"desconto\":0,\"produtos\":{\"precoUnitario\":113.19,\"idProduto\":\"PAC0000121\",\"desconto\":0,\"valorTabela\":113.19,\"quantidade\":30},\"idCondicaoPagamento\":20,\"frete\":360,\"idEmpresa\":2,\"tipoPedido\":9,\"codVendedor\":65,\"idPedido\":52,\"idFormaPagamento\":\"BB-RC-BOL-1199\"}"

    val json = "{\n" +
            "   \"idCliente\":\"\",\n" +
            "   \"idEmpresa\":\"2\",\n" +
            "   \"idFormaPagamento\":\"avista\",\n" +
            "   \"idCondicaoPagamento\":\"15\",\n" +
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

}
