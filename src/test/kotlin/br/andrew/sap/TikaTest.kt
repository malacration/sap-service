package br.andrew.sap

import org.apache.tika.Tika
import org.junit.jupiter.api.Test
import org.springframework.util.MimeType

class TikaTest {

    @Test
    fun testTxt(){
        val retorno  = Tika().detect("windson".toByteArray())
        print(retorno)
        MimeType.valueOf(retorno).type
    }
}