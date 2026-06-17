package br.andrew.sap.services.sysfeed

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SysfeedIntegratorClientTest {
    private val client = SysfeedIntegratorClient(ObjectMapper(), "http://localhost:8181")

    @Test
    fun `lookup de ordem nao deve considerar corpo vazio como existente`() {
        assertFalse(client.receivingOrderLookupFound("1445941", ""))
        assertFalse(client.receivingOrderLookupFound("1445941", "{}"))
        assertFalse(client.receivingOrderLookupFound("1445941", "[]"))
        assertFalse(client.receivingOrderLookupFound("1445941", "null"))
    }

    @Test
    fun `lookup de ordem deve considerar existente quando corpo contem nr producao`() {
        assertTrue(client.receivingOrderLookupFound("1445941", "{\"nrProducao\":\"1445941\"}"))
    }
}
