package br.andrew.sap.json

import br.andrew.sap.model.sap.documents.base.Installment
import com.fasterxml.jackson.databind.ObjectMapper
<<<<<<< HEAD
=======
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
>>>>>>> e848f19f65c789d7ec1ccb855c5e1ba198b6c15b
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
<<<<<<< HEAD
import java.util.*

class InstallmentJsonTests {


    @Test
    fun teste(){
        val json = "{\"DueDate\":\"2025-01-15\",\"Total\":1200}"
        val mapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .setTimeZone(TimeZone.getDefault())
        val obj = mapper.readValue(json, jacksonTypeRef<Installment>())

        Assertions.assertEquals("2025-01-15",obj.dueDate)
    }
}
=======

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
>>>>>>> e848f19f65c789d7ec1ccb855c5e1ba198b6c15b
