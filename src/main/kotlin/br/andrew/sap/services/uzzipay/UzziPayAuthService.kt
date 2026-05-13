package br.andrew.sap.services.uzzipay

import br.andrew.sap.infrastructure.configurations.uzzipay.UzziPayEnvrioment
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.Instant
import java.util.Date

@Service
class UzziPayAuthService(
    private val restTemplate: RestTemplate,
    private val envrioment: UzziPayEnvrioment
) {
    private var token: String? = null
    private var expiresAt: Instant? = null

    fun getToken(): String {
        if (token == null || isExpired()) {
            refreshToken()
        }
        return token!!
    }

    private fun isExpired(): Boolean {
        val expiration = expiresAt ?: return false
        return Instant.now().isAfter(expiration.minusSeconds(30))
    }

    private fun refreshToken() {
        val form = LinkedMultiValueMap<String, String>()
        val grantType = envrioment.authGrantType?.takeIf { it.isNotBlank() }
            ?: throw Exception("uzzipay.authGrantType nao configurado")
        form.add("grant_type", grantType)

        envrioment.authClientId?.takeIf { it.isNotBlank() }?.let { form.add("client_id", it) }
        envrioment.authClientSecret?.takeIf { it.isNotBlank() }?.let { form.add("client_secret", it) }
        if (grantType == "password") {
            envrioment.authUsername?.takeIf { it.isNotBlank() }?.let { form.add("username", it) }
            envrioment.authPassword?.takeIf { it.isNotBlank() }?.let { form.add("password", it) }
        }
        envrioment.authScope?.takeIf { it.isNotBlank() }?.let { form.add("scope", it) }

        val request = RequestEntity
            .post(envrioment.buildAuthUrl())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)

        val response = restTemplate.exchange(request, UzziPayTokenResponse::class.java).body
            ?: throw Exception("Nao foi possivel obter token da UzziPay")
        token = response.tokenValue()
        expiresAt = response.expiresIn?.let { Instant.ofEpochMilli(Date().time + (it * 1000)) }
    }
}

data class UzziPayTokenResponse(
    @JsonProperty("access_token") val accessToken: String? = null,
    @JsonProperty("token") val token: String? = null,
    @JsonProperty("jwt") val jwt: String? = null,
    @JsonProperty("expires_in") val expiresIn: Long? = null
) {
    fun tokenValue(): String {
        return accessToken ?: token ?: jwt ?: throw Exception("Resposta de token sem access_token/token/jwt")
    }
}
