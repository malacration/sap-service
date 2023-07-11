package br.andrew.sap.model.uzzipay

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Exception

class PayerTests {


    @Test
    fun test(){
        val erro = assertThrows<Exception> {
            Payer("","","","","","","")
        }
        Assertions.assertEquals("Documento do cliente nao pode ser vazio",erro.message)

    }
}