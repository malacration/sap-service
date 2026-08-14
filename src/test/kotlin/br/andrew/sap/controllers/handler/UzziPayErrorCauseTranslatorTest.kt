package br.andrew.sap.controllers.handler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.nio.charset.StandardCharsets

class UzziPayErrorCauseTranslatorTest {

    @Test
    fun traduzErroDeEmailDaUzzipay() {
        val body = """
            {
              "code":"BadRequest",
              "errors":[
                {
                  "field":"email",
                  "code":"Email",
                  "description":"'Email' is not a valid email address."
                }
              ],
              "traceId":"0HNNHE09LTMH7:00000003",
              "message":"Dados invalidos"
            }
        """.trimIndent()
        val cause = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            HttpHeaders.EMPTY,
            body.toByteArray(),
            StandardCharsets.UTF_8
        )
        val erro = RuntimeException("Falha de comunicacao com a uzzipay", cause)

        assertEquals(
            "E-mail do cliente invalido para gerar Pix na Uzzipay.",
            UzziPayErrorCauseTranslator.causeBy(erro)
        )
    }

    @Test
    fun retornaMensagemDaUzzipayParaErrosSemTraducao() {
        val body = """
            {
              "errors":[{"field":"amount","description":"Amount must be greater than zero."}],
              "message":"Dados invalidos"
            }
        """.trimIndent()
        val cause = HttpClientErrorException.create(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            HttpHeaders.EMPTY,
            body.toByteArray(),
            StandardCharsets.UTF_8
        )
        val erro = RuntimeException("Falha de comunicacao com a uzzipay", cause)

        assertEquals(
            "Dados invalidos: Amount must be greater than zero.",
            UzziPayErrorCauseTranslator.causeBy(erro)
        )
    }
}

