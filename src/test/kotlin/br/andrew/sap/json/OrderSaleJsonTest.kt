package br.andrew.sap.json

import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test

class OrderSaleJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val produtos = listOf<Product>(Product("PRO0001","10.5","10.0"))
        val pedido = OrderSales("CLI0001", "Date()",produtos,"2").also {
            it.u_id_pedido_forca = "666"
            it.DocumentLines.forEach{ it.U_preco_base = 555.0; it.U_preco_negociado = 123.0}
        }
        val json = mapper.writeValueAsString(pedido)
        println(json)
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"U_preco_negociado\":123.0"))
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"U_preco_base\":555.0"))
    }
}
