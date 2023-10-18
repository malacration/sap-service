package br.andrew.sap.model.telegram

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TipoMensagemTests {

    @Test
    fun geralSempreENull(){
        Assertions.assertEquals(null,TipoMensagem.geral.topic())
    }

    @Test
    fun testandoConfiguracao(){
        TipoMensagem.topics.putAll(mapOf(
            "eventos" to 1,
            "geral" to 2,
            "autorizacao" to 3,
            "testes" to 4,
            "erros" to 5
        ))
        Assertions.assertEquals(1,TipoMensagem.eventos.topic())
        Assertions.assertEquals(null,TipoMensagem.geral.topic())
        Assertions.assertEquals(3,TipoMensagem.autorizacao.topic())
        Assertions.assertEquals(4,TipoMensagem.testes.topic())
        Assertions.assertEquals(5,TipoMensagem.erros.topic())
    }
}