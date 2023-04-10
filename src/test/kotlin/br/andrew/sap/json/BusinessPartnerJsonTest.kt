package br.andrew.sap.json

import br.andrew.sap.model.BusinessPartner
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test
import java.util.*

class BusinessPartnerJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        println(mapper.writeValueAsString(BusinessPartner("windson")))
    }
}
