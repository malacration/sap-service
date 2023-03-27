package br.andrew.sap.json

import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.model.sovis.Produto
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
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
}
