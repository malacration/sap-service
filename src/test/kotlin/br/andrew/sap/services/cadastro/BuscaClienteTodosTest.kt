package br.andrew.sap.services.cadastro

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * A busca do lote de NF-e (/nota-fiscal-lote/clientes/search) roda o mesmo parceiro-full-search-text.sql
 * sem o recorte de carteira: o SQL libera tudo com SlpCode < :superVendedor, entao vai Int.MAX_VALUE.
 * A busca geral tem de continuar recortando - e ela que as telas de venda usam.
 */
class BuscaClienteTodosTest {

    private val sqlQueriesService = mock<SqlQueriesService>().also {
        whenever(it.execute(any<String>(), any<List<Parameter>>())).doReturn(OData(linkedMapOf("value" to "[]")))
    }
    private val service = BusinessPartnersService(sqlQueriesService, mock(), mock(), mock())

    // vendedor comum: superVendedor -1
    private val user = mock<User>().also {
        whenever(it.superVendedor()).doReturn(-1)
        whenever(it.principal).doReturn(30)
    }

    private fun parametros(): Map<String, String> {
        val captor = argumentCaptor<List<Parameter>>()
        org.mockito.Mockito.verify(sqlQueriesService).execute(any<String>(), captor.capture())
        return captor.firstValue.associate { it.key to it.toString().substringAfter("=") }
    }

    @Test
    fun `busca do lote libera todos os clientes`() {
        service.fullSearchTextTodosClientes("mauro", user)
        val p = parametros()
        assertEquals(Int.MAX_VALUE.toString(), p["superVendedor"])
        assertEquals("30", p["vendedor"])
        assertEquals("'%MAURO%'", p["valor"])
    }

    @Test
    fun `busca geral continua recortada pela carteira`() {
        service.fullSearchTextFallBack("mauro", user)
        assertEquals("-1", parametros()["superVendedor"])
    }
}
