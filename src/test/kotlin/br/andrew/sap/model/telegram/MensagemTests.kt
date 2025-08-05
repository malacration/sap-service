package br.andrew.sap.model.telegram

import br.andrew.sap.model.ContactOpaque
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MensagemTests {

    @Test
    fun simpleTest(){
        val msg = Mensagem("1","windson",TipoMensagem.geral)
    }

    @Test
    fun htmlTest(){
        val msg = Mensagem("1","<html> windson </html>",TipoMensagem.geral)
    }
}