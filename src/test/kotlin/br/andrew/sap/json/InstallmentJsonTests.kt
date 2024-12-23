package br.andrew.sap.json

import br.andrew.sap.model.sap.documents.base.Installment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InstallmentJsonTests {

    @Test
    fun teste() {
        val json = "{\"DueDate\":\"2025-01-15\",\"Total\":1200}"
        val mapper = ObjectMapper()
            .registerModule(KotlinModule())
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        val obj = mapper.readValue(json, jacksonTypeRef<Installment>())
        println(obj)
        Assertions.assertEquals("2025-01-15", obj.dueDate.toString())
    }
}
