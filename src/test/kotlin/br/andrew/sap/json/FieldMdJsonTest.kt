package br.andrew.sap.json

import br.andrew.sap.model.BusinessPartner
import br.andrew.sap.model.FieldMd
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class FieldMdJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val teste = mapper.writeValueAsString(FieldMd("windson","deswindson","table"))
        Assertions.assertTrue(teste.contains("\"Type\":\"db_Alpha\""))
    }
}
