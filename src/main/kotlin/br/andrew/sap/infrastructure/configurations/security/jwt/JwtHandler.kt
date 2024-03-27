package br.andrew.sap.infrastructure.configurations.security.jwt

import br.andrew.sap.infrastructure.configurations.security.otp.User
import io.jsonwebtoken.Jwts
import javax.crypto.SecretKey

class JwtHandler(jwtSecret : JwtSecretBean) {

    private val secretKey : SecretKey = jwtSecret.getKey()

    fun getUser(compactJws : String) : User {
        val clains = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(compactJws);
        return User(clains.payload.id, clains.payload.subject, listOf())
    }

    fun getToken(user : User) : String {
        return Jwts.builder().id(user.id).subject(user.name).signWith(secretKey).compact();
    }
}