package br.andrew.sap.infrastructure.configurations.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import javax.crypto.SecretKey


@Configuration
class JwtSecretBean(@Value("\${jwt.secret}") val secret : String? = null) {


    fun getKey(): SecretKey {
        return if(secret != null)
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
        else
            Jwts.SIG.HS256.key().build()
    }

}