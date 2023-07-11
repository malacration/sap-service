package br.andrew.sap.json.uzzipay

import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ResponsePixQrCodeTest{


    @Test
    fun descerializacao(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, jacksonTypeRef<DataRetonroPixQrCode>())
        Assertions.assertEquals("teste",obj.data.textContent)
    }
    val json = "{\n" +
            "   \"data\":{\n" +
            "      \"textContent\":\"teste\",\n" +
            "      \"link\":\"link\",\n" +
            "      \"reference\":\"ref\",\n" +
            "      \"image\":\"null\"\n" +
            "   }\n" +
            "}"
}