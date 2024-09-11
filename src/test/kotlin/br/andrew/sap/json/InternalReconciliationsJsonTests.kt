package br.andrew.sap.json

import br.andrew.sap.model.sap.InternalReconciliations
import br.andrew.sap.model.self.vendafutura.Contrato
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat

class InternalReconciliationsJsonTests {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val internal = InternalReconciliations()
        val json = mapper.writeValueAsString(internal)
        println(json)
        Assertions.assertTrue(json.contains("{\"ReconDate\":"))
    }
}