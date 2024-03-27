package br.andrew.sap.json

import br.andrew.sap.services.EmailAdrres
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Test

class MailJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, jacksonTypeRef<EmailAdrres>())
    }

    val json = "{ \"email\": \"windson@windson\", \"name\": \"windson\", \"type\": \"DEFAULT\"}"
}
