package br.andrew.sap.json

import br.andrew.sap.model.self.vendafutura.Contrato
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat

class ContratoJsonTests {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val contrato = Contrato(1,"code", listOf(),100.0,SimpleDateFormat("dd/MM/yyyy").parse("05/08/1992"))
        val json = mapper.writeValueAsString(contrato)
        println(json)
        Assertions.assertTrue(json.contains("\"U_dataCriacao\":\"1992-08-05\""))
    }
}