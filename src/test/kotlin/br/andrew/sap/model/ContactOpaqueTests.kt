package br.andrew.sap.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ContactOpaqueTests {

    @Test
    fun emailTest(){
        val entrada = "windson@gmail.com"
        val resultt = "w***son@gmail.com"
        val saida = ContactOpaque(entrada)
        Assertions.assertEquals(resultt.length,entrada.length)
        Assertions.assertEquals(resultt,saida.contato)
    }

    @Test
    fun telefoneTest(){
        val entrada = "699956666"
        val resultt = "699***666"
        val saida = ContactOpaque(entrada)
        Assertions.assertEquals(resultt,saida.contato)
    }

    @Test
    fun telefoneComParente(){
        val entrada = "(69)9956666"
        val resultt = "699***666"
        val saida = ContactOpaque(entrada)
        Assertions.assertEquals(resultt,saida.contato)
    }

    @Test
    fun telefoneComTraco(){
        val entrada = "(69)99956-5666"
        val resultt = "699*****666"
        val saida = ContactOpaque(entrada)
        Assertions.assertEquals(resultt,saida.contato)
    }
}