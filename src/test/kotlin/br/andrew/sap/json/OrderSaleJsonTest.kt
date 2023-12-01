package br.andrew.sap.json

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.DocEntry
import br.andrew.sap.model.documents.DocumentStatus
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.base.Product
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
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
        Assertions.assertTrue(json.contains("\"U_id_pedido_forca\":\"666\""))
        Assertions.assertTrue(json.contains("\"U_preco_negociado\":123.0"))
        Assertions.assertTrue(json.contains("\"U_preco_base\":555.0"))
        Assertions.assertTrue(json.contains("\"UnitPrice\":\"10.0\""))
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
        Assertions.assertTrue(!json.contains("U_id_Pedido_Forca"))
        Assertions.assertTrue(!json.contains("U_preco_negociado"))
        Assertions.assertTrue(!json.contains("U_preco_base"))
        Assertions.assertTrue(json.contains("\"UnitPrice\":\"10.0\""))
    }


    @Test
    fun tryJsonOdataToOrder(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val file = File("src/test/kotlin/br/andrew/sap/json/draft.json").readBytes()
        val obj = mapper.readValue(String(file), jacksonTypeRef<OData>())
        val order = obj.tryGetValue<OrderSales>()
        order.DocumentLines.forEach { Assertions.assertEquals("500.01",it.WarehouseCode) }
        Assertions.assertEquals(DocumentStatus.bost_Open,order.DocumentStatus)

    }


    @Test
    fun tryJsonSqlTest(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(jsonSql, jacksonTypeRef<OData>())
        val data = obj.tryGetValues<DocEntry>()
        Assertions.assertEquals(11993,data[0].DocEntry)
    }

    val jsonSql = "{\n" +
            "   \"odata.metadata\" : \"https://134.65.16.195:50000/b1s/v1/\$metadata#SAPB1.SQLQueryResult\",\n" +
            "   \"SqlText\" : \"SELECT  t.\\\"DocEntry\\\" FROM     \\\"OVPM\\\" t     LEFT JOIN \\\"VPM2\\\" line ON t.\\\"DocEntry\\\" = line.\\\"DocNum\\\" WHERE  line.\\\"DocEntry\\\" = :DocEntry\",\n" +
            "   \"value\" : [\n" +
            "      {\n" +
            "         \"DocEntry\" : 11993\n" +
            "      }\n" +
            "   ]\n" +
            "}"
}
