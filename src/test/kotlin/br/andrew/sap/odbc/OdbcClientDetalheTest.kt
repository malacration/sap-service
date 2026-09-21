package br.andrew.sap.odbc

import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.OdbcException
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.web.client.RestTemplateBuilder
import java.net.InetSocketAddress

/**
 * A causa real do erro precisa chegar a quem ve a tela - mas sem levar o SQL
 * junto. Suprimir tudo deixava o suporte cego: o motivo ficava so no log do
 * servidor, que foi exatamente o que aconteceu com o 400 de parametro sobrando.
 *
 * Usa o HttpServer do JDK em vez de WireMock: este projeto nao tem WireMock no
 * classpath e o teste nao justifica uma dependencia nova.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OdbcClientDetalheTest {

    private lateinit var servidor: HttpServer
    private lateinit var client: OdbcClient

    @Volatile private var status = 200
    @Volatile private var corpo = "{}"

    @BeforeAll
    fun subir() {
        servidor = HttpServer.create(InetSocketAddress(0), 0)
        servidor.createContext("/api/v1/query") { troca ->
            val bytes = corpo.toByteArray()
            troca.responseHeaders.add("Content-Type", "application/json")
            troca.sendResponseHeaders(status, bytes.size.toLong())
            troca.responseBody.use { it.write(bytes) }
        }
        servidor.start()
        client = OdbcClient(RestTemplateBuilder(), "http://localhost:${servidor.address.port}", "k", 30)
    }

    @AfterAll
    fun derrubar() = servidor.stop(0)

    private fun responde(codigo: Int, json: String) {
        status = codigo
        corpo = json
    }

    private fun consultar() = assertThrows(OdbcException::class.java) {
        client.consultar("SELECT 1 FROM T", mapOf(), 10)
    }

    @Test
    fun `erro de validacao expoe a causa tecnica`() {
        // Este texto nao contem SQL nem dado de cliente - e bug na montagem da
        // consulta, e esconde-lo so atrasa o diagnostico.
        responde(400, """{"erro":"sql_invalido","mensagem":"Parametros nao referenciados na consulta: dataFimAnterior."}""")
        val ex = consultar()
        assertEquals("sql_invalido", ex.codigo)
        assertNotNull(ex.detalhe)
        assertTrue(ex.detalhe!!.contains("dataFimAnterior"), ex.detalhe!!)
    }

    @Test
    fun `erro vindo do banco NAO expoe detalhe`() {
        // Aqui a mensagem pode ecoar a instrucao e nomes de tabela.
        responde(502, """{"erro":"erro_banco","mensagem":"erro perto de SELECT SENHA FROM SEGREDO"}""")
        val ex = consultar()
        assertNull(ex.detalhe, "detalhe de erro de banco pode vazar SQL")
        assertFalse(ex.message.contains("SEGREDO"))
    }

    @Test
    fun `rota errada diz o que verificar`() {
        responde(404, """{"erro":"rota_nao_encontrada","mensagem":"Rota nao encontrada: /api/v1/qury."}""")
        val ex = consultar()
        assertTrue(ex.message.contains("odbc.base-url"), ex.message)
        assertTrue(ex.detalhe!!.contains("/api/v1/qury"))
    }
}
