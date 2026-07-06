package br.andrew.sap.infrastructure.security.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.Locator
import io.jsonwebtoken.security.Jwks
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.Key
import java.util.Base64

/**
 * Valida access tokens emitidos pelo Keycloak.
 *
 * As chaves publicas do realm sao obtidas via JWKS e ficam em cache em
 * memoria, sendo recarregadas quando aparece um `kid` desconhecido (rotacao
 * de chaves).
 */
@Service
class KeycloakJwtService(private val properties: KeycloakProperties) {

    private val log = LoggerFactory.getLogger(KeycloakJwtService::class.java)
    private val httpClient = OkHttpClient()
    private val mapper = ObjectMapper()

    @Volatile
    private var keys: Map<String, Key> = emptyMap()

    /**
     * Decide, sem validar a assinatura, se o token e candidato a Keycloak,
     * olhando o algoritmo no header: tokens internos (OTP/`/logar`) usam HMAC
     * (HS256), enquanto o Keycloak usa chave assimetrica (RS256). Isso roteia
     * o token independente de config de issuer; o issuer exato e exigido
     * depois em [validate]. Tokens HS* sao deixados para o filtro interno.
     */
    fun looksLikeKeycloakToken(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return false
            val header = String(Base64.getUrlDecoder().decode(parts[0]))
            val alg = mapper.readTree(header).get("alg")?.asText() ?: return false
            !alg.uppercase().startsWith("HS")
        } catch (e: Exception) {
            false
        }
    }

    /** Valida assinatura, issuer e expiracao, retornando os claims. */
    fun validate(token: String): Claims {
        return try {
            parse(token)
        } catch (e: Exception) {
            // pode ser rotacao de chave: recarrega o JWKS e tenta uma vez mais
            log.warn("Falha ao validar token Keycloak, recarregando JWKS", e)
            refreshKeys()
            parse(token)
        }
    }

    private fun parse(token: String): Claims {
        if (keys.isEmpty()) refreshKeys()
        return Jwts.parser()
            .keyLocator(keyLocator)
            .requireIssuer(properties.issuer())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private val keyLocator = Locator<Key> { header ->
        val kid = (header as? JwsHeader)?.keyId
        val cached = kid?.let { keys[it] }
        if (cached != null) cached
        else {
            refreshKeys()
            kid?.let { keys[it] }
        }
    }

    @Synchronized
    private fun refreshKeys() {
        val request = Request.Builder().url(properties.jwksUri()).get().build()
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string()
                ?: throw IllegalStateException("JWKS vazio em ${properties.jwksUri()}")
            val jwkSet = Jwks.setParser().build().parse(body)
            keys = jwkSet.getKeys().associate { jwk -> jwk.id to jwk.toKey() as Key }
            log.info("JWKS do Keycloak carregado: {} chave(s)", keys.size)
        }
    }
}
