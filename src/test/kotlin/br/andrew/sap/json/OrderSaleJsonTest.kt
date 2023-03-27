package br.andrew.sap.json

import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.model.sovis.Produto
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test
import java.util.*

class OrderSaleJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val produtos = listOf<Product>(Product("PRO0001","10.5","10.0"))
        val pedido = OrderSales("CLI0001", Date(),produtos,"2",
                "avista")
        println(mapper.writeValueAsString(pedido))
    }
}
