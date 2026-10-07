package br.andrew.sap.services.autorizacao

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ClienteEstouraLimiteCreditoRegraTest {

    private val odbc = mock<OdbcClient>()
    private val regra = ClienteEstouraLimiteCreditoRegra(odbc)

    private fun pedido(total: Double, avista: Boolean = false) = mock<Document>().also {
        whenever(it.CardCode).doReturn("C001")
        whenever(it.isAvista()).doReturn(avista)
        whenever(it.totalLiquido()).doReturn(total)
    }

    //NUMERIC do HANA chega como String pelo sap-odbc
    private fun saldo(limite: String, divida: String) {
        whenever(odbc.consultar(any(), any(), any())).doReturn(QueryResponse(rows = listOf(
            mapOf("CardCode" to "C001", "LimiteCredito" to limite, "DividaAberta" to divida))))
    }

    @Test
    fun `cliente pontual com titulos a vencer no teto do limite vai para autorizacao`() {
        //900 a vencer + 200 do pedido passa do limite de 1000
        saldo("1000", "900")
        assertTrue(regra.avalia(pedido(200.0)))
    }

    @Test
    fun `saldo mais pedido dentro do limite passa`() {
        saldo("1000", "700")
        assertFalse(regra.avalia(pedido(200.0)))
    }

    @Test
    fun `cliente sem divida com pedido acima do limite vai para autorizacao`() {
        saldo("1000", "0")
        assertTrue(regra.avalia(pedido(1500.0)))
    }

    @Test
    fun `credito do cliente em aberto reduz a exposicao`() {
        saldo("1000", "-300")
        assertFalse(regra.avalia(pedido(1200.0)))
    }

    @Test
    fun `limite zerado e venda a vista nao sao avaliados`() {
        saldo("0", "5000")
        assertFalse(regra.avalia(pedido(200.0)))
        saldo("1000", "5000")
        assertFalse(regra.avalia(pedido(200.0, avista = true)))
    }

    @Test
    fun `cliente inexistente ou sap-odbc fora do ar nao retem o pedido`() {
        whenever(odbc.consultar(any(), any(), any())).doReturn(QueryResponse(rows = emptyList()))
        assertFalse(regra.avalia(pedido(200.0)))
        whenever(odbc.consultar(any(), any(), any())).doThrow(RuntimeException("fora do ar"))
        assertFalse(regra.avalia(pedido(200.0)))
    }
}
