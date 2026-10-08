package br.andrew.sap.security.acesso

import br.andrew.sap.infrastructure.security.keycloak.KeycloakAdminClient
import br.andrew.sap.infrastructure.security.keycloak.KeycloakAdminException
import br.andrew.sap.infrastructure.security.keycloak.KeycloakProperties
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList

/**
 * O cliente de administracao contra um Keycloak de mentira (servidor HTTP do JDK): o que ele envia
 * (client_credentials, bearer, nome da role) e como reage a cada resposta.
 */
class KeycloakAdminClientTest {

    private class Chamada(val metodo: String, val caminho: String, val corpo: String, val auth: String?)

    private val chamadas = CopyOnWriteArrayList<Chamada>()
    private var statusDaRole = 201
    private var statusDoToken = 200
    private var clientsJson = """[{"id":"uuid-sapvema","clientId":"sapvema"}]"""
    /** Quantas respostas 401 dar na criacao da role antes de aceitar (token revogado). */
    private var rejeitosDeToken = 0
    private var tokenN = 0
    /** UUID que o Keycloak ja nao conhece (client recriado): POST nele responde 404. */
    private var uuidInvalido: String? = null
    private var statusDaRoleDeRealm = 404
    private var corpoDoToken: String? = null

    private val servidor: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).also { s ->
        s.createContext("/") { ex -> responder(ex) }
        s.start()
    }

    @AfterEach
    fun parar() = servidor.stop(0)

    private fun responder(ex: HttpExchange) {
        val corpo = ex.requestBody.readAllBytes().toString(Charsets.UTF_8)
        val caminho = ex.requestURI.rawPath + (ex.requestURI.rawQuery?.let { "?$it" } ?: "")
        chamadas.add(Chamada(ex.requestMethod, caminho, corpo, ex.requestHeaders.getFirst("Authorization")))
        val (status, resposta) = when {
            caminho == "/realms/rovema/protocol/openid-connect/token" ->
                if (statusDoToken == 200) 200 to (corpoDoToken ?: """{"access_token":"token-${++tokenN}","expires_in":300}""") else statusDoToken to "{}"
            caminho.startsWith("/admin/realms/rovema/roles/") && ex.requestMethod == "GET" -> statusDaRoleDeRealm to "{}"
            caminho.endsWith("/roles") && ex.requestMethod == "POST" && uuidInvalido != null && caminho.contains("/clients/$uuidInvalido/") -> 404 to "{}"
            caminho.startsWith("/admin/realms/rovema/clients?") -> 200 to clientsJson
            caminho.endsWith("/roles") && ex.requestMethod == "POST" ->
                if (rejeitosDeToken > 0) { rejeitosDeToken--; 401 to "{}" } else statusDaRole to "{}"
            else -> 404 to "{}"
        }
        val bytes = resposta.toByteArray()
        ex.sendResponseHeaders(status, if (status == 201) -1 else bytes.size.toLong())
        if (status != 201) ex.responseBody.use { it.write(bytes) } else ex.close()
    }

    private fun cliente(): KeycloakAdminClient {
        val p = KeycloakProperties().also {
            it.url = "http://127.0.0.1:${servidor.address.port}"
            it.realm = "rovema"
            it.clientId = "sapvema"
            it.admin.enabled = true
            it.admin.clientId = "sapvema-admin"
            it.admin.clientSecret = "segredo-123"
            it.admin.timeoutMs = 3000
        }
        return KeycloakAdminClient(p)
    }

    private fun postsDeRole() = chamadas.filter { it.metodo == "POST" && it.caminho.endsWith("/roles") }
    private fun logins() = chamadas.filter { it.caminho.endsWith("/openid-connect/token") }

    @Test
    fun `cria a role de client com client_credentials e bearer`() {
        assertTrue(cliente().garantirRole("conferente"))

        val login = logins().single()
        assertTrue(login.corpo.contains("grant_type=client_credentials"))
        assertTrue(login.corpo.contains("client_id=sapvema-admin"))
        assertTrue(login.corpo.contains("client_secret=segredo-123"))

        val lookup = chamadas.first { it.caminho.startsWith("/admin/realms/rovema/clients?") }
        assertEquals("/admin/realms/rovema/clients?clientId=sapvema", lookup.caminho)
        assertEquals("Bearer token-1", lookup.auth)

        val post = postsDeRole().single()
        assertEquals("/admin/realms/rovema/clients/uuid-sapvema/roles", post.caminho)
        assertEquals("Bearer token-1", post.auth)
        assertTrue(post.corpo.contains("\"name\":\"conferente\""))
    }

    @Test
    fun `role que ja existe nao e erro`() {
        statusDaRole = 409
        assertFalse(cliente().garantirRole("cobranca"))
    }

    @Test
    fun `token e uuid do client sao reaproveitados entre chamadas`() {
        val c = cliente()
        c.garantirRole("a")
        c.garantirRole("b")

        assertEquals(1, logins().size)
        assertEquals(1, chamadas.count { it.caminho.startsWith("/admin/realms/rovema/clients?") })
        assertEquals(2, postsDeRole().size)
    }

    @Test
    fun `token revogado (401) renova o login e repete uma vez`() {
        rejeitosDeToken = 1

        assertTrue(cliente().garantirRole("conferente"))

        assertEquals(2, logins().size)
        assertEquals(listOf("Bearer token-1", "Bearer token-2"), postsDeRole().map { it.auth })
    }

    @Test
    fun `401 duas vezes seguidas falha em vez de repetir sem fim`() {
        rejeitosDeToken = 5
        statusDaRole = 201
        assertThrows(KeycloakAdminException::class.java) { cliente().garantirRole("conferente") }
        assertEquals(2, postsDeRole().size)
    }

    @Test
    fun `erro do keycloak vira excecao com o status`() {
        statusDaRole = 403
        val erro = assertThrows(KeycloakAdminException::class.java) { cliente().garantirRole("conferente") }
        assertTrue(erro.message!!.contains("403"))
    }

    @Test
    fun `login recusado e client inexistente dizem o que falta`() {
        statusDoToken = 401
        assertTrue(assertThrows(KeycloakAdminException::class.java) { cliente().garantirRole("a") }.message!!.contains("sapvema-admin"))

        statusDoToken = 200
        clientsJson = "[]"
        assertTrue(assertThrows(KeycloakAdminException::class.java) { cliente().garantirRole("a") }.message!!.contains("nao existe"))
    }

    @Test
    fun `keycloak fora do ar vira excecao tratavel`() {
        val c = cliente()
        servidor.stop(0)
        assertThrows(KeycloakAdminException::class.java) { c.garantirRole("a") }
    }

    @Test
    fun `client recriado no keycloak - o uuid guardado e refeito uma vez`() {
        val c = cliente()
        c.garantirRole("a")
        uuidInvalido = "uuid-sapvema"
        clientsJson = """[{"id":"uuid-novo","clientId":"sapvema"}]"""

        assertTrue(c.garantirRole("b"))

        assertEquals("/admin/realms/rovema/clients/uuid-novo/roles", postsDeRole().last().caminho)
        assertEquals(2, chamadas.count { it.caminho.startsWith("/admin/realms/rovema/clients?") })
    }

    @Test
    fun `404 que persiste depois de refazer a busca falha, sem repetir sem fim`() {
        val c = cliente()
        c.garantirRole("a")
        uuidInvalido = "uuid-sapvema"

        assertThrows(KeycloakAdminException::class.java) { c.garantirRole("b") }
        assertEquals(3, postsDeRole().size)
    }

    @Test
    fun `role de realm com o mesmo nome e detectada, ausente e desconhecida tambem`() {
        val c = cliente()
        statusDaRoleDeRealm = 200
        assertEquals(true, c.roleDeRealmExiste("vendedor"))
        statusDaRoleDeRealm = 404
        assertEquals(false, c.roleDeRealmExiste("novo"))
        statusDaRoleDeRealm = 403
        assertEquals(null, c.roleDeRealmExiste("novo"))
        assertTrue(chamadas.any { it.caminho == "/admin/realms/rovema/roles/vendedor" })
    }

    @Test
    fun `resposta em formato inesperado nao vaza o conteudo na excecao`() {
        corpoDoToken = "<html>access_token=SEGREDO-NO-CORPO</html>"
        val erro = assertThrows(KeycloakAdminException::class.java) { cliente().garantirRole("a") }
        assertFalse(erro.message!!.contains("SEGREDO"))
        assertEquals("Resposta do Keycloak em formato inesperado", erro.message)
    }

    @Test
    fun `nome que poderia mudar o caminho da URL e recusado antes de chamar`() {
        val c = cliente()
        listOf("../clients", "a/b", "A", "x y", "").forEach {
            assertThrows(IllegalArgumentException::class.java, { c.garantirRole(it) }, it)
        }
        assertTrue(chamadas.isEmpty())
    }
}
