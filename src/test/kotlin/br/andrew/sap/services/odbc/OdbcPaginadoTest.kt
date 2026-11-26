package br.andrew.sap.services.odbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer
import org.springframework.data.domain.PageRequest

class OdbcPaginadoTest {

    private val chamadas = mutableListOf<Pair<Map<String, Any?>, Int>>()

    /** Falso que devolve [linhas] na consulta da pagina e [total] na re-consulta pela primeira linha (fim = 1). */
    private fun odbc(linhas: List<Map<String, Any?>>, total: Long, primeiraPagina: Int = 1) = mock(OdbcClient::class.java, Answer { inv ->
        if (inv.method.name != "consultar") return@Answer null
        @Suppress("UNCHECKED_CAST") val p = inv.arguments[1] as Map<String, Any?>
        chamadas.add(p to inv.arguments[2] as Int)
        val rows = if (p["fim"] == 1L) List(primeiraPagina) { mapOf("Total" to total) } else linhas
        // Como o sap-odbc: mais linhas que maxRows e resultado_truncado.
        if (rows.size > inv.arguments[2] as Int) throw OdbcException("resultado_truncado", "truncado")
        QueryResponse(rows = rows)
    })

    @Test
    fun `tamanho e limitado ao teto e o intervalo vai como inicio e fim`() {
        val pagina = odbc(List(130) { mapOf("Total" to "130") }, 0).consultarPagina("sql", mapOf("a" to 1), PageRequest.of(0, 5000), 200)

        val (params, maxRows) = chamadas.single()
        assertEquals(0L, params["inicio"])
        assertEquals(200L, params["fim"])
        assertEquals(1, params["a"])
        assertEquals(200, maxRows)
        assertEquals(200, pagina.size)
        assertEquals(130L, pagina.totalElements)
    }

    @Test
    fun `maxRows explicito vale para modos agrupados`() {
        odbc(listOf(mapOf("Total" to 20)), 0).consultarPagina("sql", mapOf(), PageRequest.of(2, 5), 200, 10_000)
        assertEquals(10L, chamadas.single().first["inicio"])
        assertEquals(15L, chamadas.single().first["fim"])
        assertEquals(10_000, chamadas.single().second)
    }

    @Test
    fun `primeira pagina vazia tem total zero sem re-consulta`() {
        val pagina = odbc(listOf(), 7).consultarPagina("sql", mapOf(), PageRequest.of(0, 50), 200)
        assertEquals(0L, pagina.totalElements)
        assertEquals(1, chamadas.size)
    }

    @Test
    fun `pagina alem da ultima conta de novo pela primeira linha`() {
        val pagina = odbc(listOf(), 7).consultarPagina("sql", mapOf(), PageRequest.of(3, 50), 200)

        assertEquals(7L, pagina.totalElements)
        assertEquals(0L, chamadas.last().first["inicio"])
        assertEquals(1L, chamadas.last().first["fim"])
        assertEquals(1, chamadas.last().second)
    }

    @Test
    fun `pagina alem da ultima em modo agrupado nao trunca com o primeiro grupo de varias linhas`() {
        val pagina = odbc(listOf(), 7, primeiraPagina = 3).consultarPagina("sql", mapOf(), PageRequest.of(3, 50), 200, 10_000)

        assertEquals(0, pagina.content.size)
        assertEquals(7L, pagina.totalElements)
        assertEquals(10_000, chamadas.last().second)
    }
}
