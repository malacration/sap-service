package br.andrew.sap.infrastructure.security.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * API de administracao do Keycloak, so para criar a role de client quando um perfil novo e criado na
 * tela de Regras de Acesso. So existe com `keycloak.admin.enabled=true`.
 *
 * As roles do sistema sao roles do client [KeycloakProperties.clientId] (e o que o
 * KeycloakUserMapper le em `resource_access`), por isso a role nasce nele, sem prefixo.
 *
 * Usa um client confidencial com service account (client_credentials); o token e guardado ate
 * perto de expirar. Chamadas com timeout proprio: o RestTemplate compartilhado nao tem.
 */
@Service
@ConditionalOnProperty(value = ["keycloak.admin.enabled"], havingValue = "true", matchIfMissing = false)
class KeycloakAdminClient(private val properties: KeycloakProperties) : KeycloakRolesGateway {

    private val log = LoggerFactory.getLogger(KeycloakAdminClient::class.java)
    private val mapper = ObjectMapper()
    private val json = "application/json".toMediaType()
    private val http = OkHttpClient.Builder()
        .callTimeout(Duration.ofMillis(properties.admin.timeoutMs))
        .build()

    private var token: String? = null
    private var tokenValidoAte: Long = 0
    private var clientUuid: String? = null

    @Synchronized
    override fun garantirRole(nome: String): Boolean {
        require(nome.matches(NOME_SEGURO)) { "Nome de role invalido para o Keycloak: '$nome'" }
        val corpo = mapper.writeValueAsString(
            mapOf("name" to nome, "description" to "Criada pela tela Regras de Acesso")
        ).toRequestBody(json)
        var refezBusca = false
        while (true) {
            val usouCache = clientUuid != null
            val uuid = uuidDoClient()
            val codigo = chamar { t ->
                Request.Builder().url(admin("clients/$uuid/roles")).header("Authorization", "Bearer $t").post(corpo).build()
            }.use { it.code }
            when {
                codigo == 201 -> return true.also { log.info("Role '{}' criada no client {} do Keycloak", nome, properties.clientId) }
                codigo == 409 -> return false
                // O client foi recriado com o mesmo clientId: o UUID guardado nao existe mais. Busca de novo, uma vez.
                codigo == 404 && usouCache && !refezBusca -> {
                    clientUuid = null
                    refezBusca = true
                }
                else -> throw KeycloakAdminException("Keycloak recusou criar a role '$nome': HTTP $codigo")
            }
        }
    }

    @Synchronized
    override fun roleDeRealmExiste(nome: String): Boolean? {
        require(nome.matches(NOME_SEGURO)) { "Nome de role invalido para o Keycloak: '$nome'" }
        return try {
            val codigo = chamar { t -> Request.Builder().url(admin("roles/$nome")).header("Authorization", "Bearer $t").get().build() }
                .use { it.code }
            when (codigo) {
                200 -> true
                404 -> false
                else -> null.also { log.info("Nao foi possivel verificar a role de realm '{}': HTTP {}", nome, codigo) }
            }
        } catch (e: KeycloakAdminException) {
            log.info("Nao foi possivel verificar a role de realm '{}': {}", nome, e.message)
            null
        }
    }

    private fun uuidDoClient(): String {
        clientUuid?.let { return it }
        val url = admin("clients").newBuilder().addQueryParameter("clientId", properties.clientId).build()
        val resposta = chamar { t -> Request.Builder().url(url).header("Authorization", "Bearer $t").get().build() }
        val achados = resposta.use {
            if (it.code != 200) throw KeycloakAdminException("Keycloak nao listou o client '${properties.clientId}': HTTP ${it.code}")
            lerJson(it)
        }
        val id = achados.firstOrNull()?.get("id")?.asText()
            ?: throw KeycloakAdminException("O client '${properties.clientId}' nao existe no realm '${properties.realm}'")
        return id.also { clientUuid = it }
    }

    /** Uma tentativa com o token em cache; se o Keycloak responder 401 (token revogado), renova e repete uma vez. */
    private fun chamar(montar: (String) -> Request): Response {
        val primeira = executar(montar(tokenValido()))
        if (primeira.code != 401) return primeira
        primeira.close()
        token = null
        return executar(montar(tokenValido()))
    }

    private fun executar(request: Request): Response =
        try {
            http.newCall(request).execute()
        } catch (e: Exception) {
            throw KeycloakAdminException("Nao foi possivel falar com o Keycloak: ${e.message}", e)
        }

    private fun tokenValido(): String {
        val atual = token
        if (atual != null && System.currentTimeMillis() < tokenValidoAte) return atual
        val form = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("client_id", properties.admin.clientId)
            .add("client_secret", properties.admin.clientSecret)
            .build()
        val request = Request.Builder()
            .url("${properties.issuer()}/protocol/openid-connect/token".toHttpUrl())
            .post(form).build()
        val corpo = executar(request).use {
            if (it.code != 200)
                throw KeycloakAdminException("Keycloak recusou o login do client de administracao '${properties.admin.clientId}': HTTP ${it.code}")
            lerJson(it)
        }
        val novo = corpo.get("access_token")?.asText()
            ?: throw KeycloakAdminException("Resposta do Keycloak sem access_token")
        val segundos = corpo.get("expires_in")?.asLong() ?: 60
        tokenValidoAte = System.currentTimeMillis() + (segundos - 15).coerceAtLeast(5) * 1000
        return novo.also { token = it }
    }

    /** O erro do Jackson cita um trecho do corpo (que pode ser o token): nunca sobe, vira uma mensagem fixa. */
    private fun lerJson(resposta: Response): com.fasterxml.jackson.databind.JsonNode =
        try {
            mapper.readTree(resposta.body?.string() ?: "")
        } catch (e: Exception) {
            throw KeycloakAdminException("Resposta do Keycloak em formato inesperado")
        }

    private fun admin(caminho: String) =
        "${properties.url.trimEnd('/')}/admin/realms/${properties.realm}/$caminho".toHttpUrl()

    private companion object {
        /** O mesmo formato que o validador aceita para perfis: nada que possa mudar o caminho da URL. */
        val NOME_SEGURO = Regex("^[a-z0-9][a-z0-9_-]*$")
    }
}
