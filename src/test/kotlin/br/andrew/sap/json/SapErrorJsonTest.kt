package br.andrew.sap.json

import br.andrew.sap.model.SapError
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test

class SapErrorJsonTest {


    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, SapError::class.java)
    }

    val json = "{\n" +
            "   \"error\" : {\n" +
            "      \"code\" : -5002,\n" +
            "      \"message\" : {\n" +
            "         \"lang\" : \"en-us\",\n" +
            "         \"value\" : \"1250000129 - Linked payment method BB-RC-BOL-1199 is inactive or is no longer linked with business partner CLI0000001\"\n" +
            "      }\n" +
            "   }\n" +
            "}\n"
}