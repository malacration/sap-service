package br.andrew.sap.services.batch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BatchRespondeHandlerTest {

    private val handler = BatchRespondeHandler()

    @Test
    fun `extrai mensagem com aspas escapadas ignorando details vazio`() {
        val saida = """--batchresponse_test
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 2

HTTP/1.1 400 Bad Request
Content-Type: application/json;charset=utf-8
Content-Length: 266
OData-Version: 4.0

{
   "error" : {
      "code" : "-5002",
      "details" : [
         {
            "code" : "",
            "message" : ""
         }
      ],
      "message" : "242000043 - This document row does not support the \"Free of Charge Business Partner\" feature"
   }
}
--batchresponse_test--"""

        val resultado = handler.parseBatchResponse(saida)

        assertEquals(1, resultado.size)
        val response = resultado.first()
        assertFalse(response.success)
        assertEquals(400, response.statusCode)
        assertEquals(2, response.contentId)
        assertEquals("-5002", response.errorCode)
        assertEquals(
            "242000043 - This document row does not support the \"Free of Charge Business Partner\" feature",
            response.errorMessage
        )
    }

    @Test
    fun `nao retorna mensagem vazia do no details`() {
        val saida = """--batchresponse_test
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 2

HTTP/1.1 404 Not Found
Content-Type: application/json;charset=utf-8
Content-Length: 210
OData-Version: 4.0

{
   "error" : {
      "code" : "-2028",
      "details" : [
         {
            "code" : "",
            "message" : ""
         }
      ],
      "message" : "No matching records found (ODBC -2028)"
   }
}
--batchresponse_test--"""

        val resultado = handler.parseBatchResponse(saida)

        assertEquals(1, resultado.size)
        assertEquals("-2028", resultado.first().errorCode)
        assertEquals("No matching records found (ODBC -2028)", resultado.first().errorMessage)
    }

    @Test
    fun `extrai value quando message vem como objeto`() {
        val saida = """--batchresponse_test
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 1

HTTP/1.1 400 Bad Request
Content-Type: application/json;charset=utf-8

{
   "error" : {
      "code" : "-10",
      "message" : { "lang" : "pt-br", "value" : "Mensagem detalhada do SAP" }
   }
}
--batchresponse_test--"""

        val resultado = handler.parseBatchResponse(saida)

        assertEquals(1, resultado.size)
        assertEquals("-10", resultado.first().errorCode)
        assertEquals("Mensagem detalhada do SAP", resultado.first().errorMessage)
    }

    @Test
    fun `resposta de sucesso nao gera erro`() {
        val saida = """--batchresponse_test
Content-Type: application/http
Content-Transfer-Encoding: binary
Content-ID: 1

HTTP/1.1 204 No Content


--batchresponse_test--"""

        val resultado = handler.parseBatchResponse(saida)

        assertEquals(1, resultado.size)
        assertTrue(resultado.first().success)
        assertEquals(204, resultado.first().statusCode)
        assertNull(resultado.first().errorMessage)
    }
}
