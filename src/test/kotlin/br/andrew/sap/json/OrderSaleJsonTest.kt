package br.andrew.sap.json

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Test
import java.io.File

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
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"U_Id_Pedido_Forca\":\"666\""))
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"U_preco_negociado\":123.0"))
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("\"U_preco_base\":555.0"))
    }

    @Test
    fun valuesNull(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val produtos = listOf<Product>(Product("PRO0001","10.5","10.0"))
        val pedido = OrderSales("CLI0001", "Date()",produtos,"2").also {
            it.DocumentLines.forEach{ it.U_preco_base = null; it.U_preco_negociado = null}
        }
        val json = mapper.writeValueAsString(pedido)
        println(json)
        org.junit.jupiter.api.Assertions.assertTrue(!json.contains("U_Id_Pedido_Forca"))
        org.junit.jupiter.api.Assertions.assertTrue(!json.contains("U_preco_negociado"))
        org.junit.jupiter.api.Assertions.assertTrue(!json.contains("U_preco_base"))
    }


    @Test
    fun tryJsonOdataToOrder(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val file = File("src/test/kotlin/br/andrew/sap/json/draft.json").readBytes()
        val obj = mapper.readValue(String(file), jacksonTypeRef<OData>())
        obj.tryGetValue<OrderSales>()
    }
}
