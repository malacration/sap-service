package br.andrew.sap.json

import br.andrew.sap.model.sap.documents.base.Installment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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