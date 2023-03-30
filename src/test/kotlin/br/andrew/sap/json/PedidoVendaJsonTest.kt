package br.andrew.sap.json

import br.andrew.sap.model.romaneio.RomaneioEntradaInsumo
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.model.sovis.Produto
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Test

class PedidoVendaJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val produtos = listOf<Produto>(Produto("PRO0001",10.5,10.0))
        val pedido = PedidoVenda("CLI0001","18",
                "avista",
                "15", produtos,"2")
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
            "   \"idCliente\":\"CLI0002777\",\n" +
            "   \"produtos\":{\n" +
            "      \"precoUnitario\":162.3362,\n" +
            "      \"idproduto\":\"PAC0000118\",\n" +
            "      \"quantidade\":4\n" +
            "   },\n" +
            "   \"idCondicaoPagamento\":\"1\",\n" +
            "   \"idEmpresa\":2,\n" +
            "   \"idFormaPagamento\":\"AVISTA\"\n" +
            "}"
}
