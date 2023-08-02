package br.andrew.sap.json

import br.andrew.sap.model.bank.Payment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PaymentTestJson {


    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val saida = mapper.writeValueAsString(
            Payment().also {
                it.setBPID("666")
                it.U_pix_reference = "1234567890"
            })
        Assertions.assertTrue(saida.contains("U_pix_reference"))
        println(saida)
    }
}