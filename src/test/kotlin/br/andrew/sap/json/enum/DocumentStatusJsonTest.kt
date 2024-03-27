package br.andrew.sap.json.enum

import br.andrew.sap.model.documents.DocumentStatus
import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.partner.BusinessPartner
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.json.JsonTest

class DocumentStatusJsonTest {

    @Test
    fun createJson(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val saida = mapper.writeValueAsString(
            DocumentStatus.bost_Close)
        println(saida)
    }

    @Test
    fun readJson(){
        val json = "\"bost_Close\""
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, jacksonTypeRef<DocumentStatus>())
        Assertions.assertEquals(obj,DocumentStatus.bost_Close)
    }
}