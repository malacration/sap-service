package br.andrew.sap.infrastructure.security.jwt

import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import javax.crypto.SecretKey


@Configuration
class JwtSecretBean(@Value("\${jwt.secret}") val secret : String) {


    fun getKey(): SecretKey {
        return Keys.hmacShaKeyFor(secret.toByteArray())
    }
}