package br.andrew.sap.infrastructure.security.jwt

import br.andrew.sap.model.authentication.User
import io.jsonwebtoken.Jwts
import org.springframework.security.core.authority.SimpleGrantedAuthority
import javax.crypto.SecretKey

class JwtHandler(jwtSecret : JwtSecretBean) {

    private val secretKey : SecretKey = jwtSecret.getKey()

    fun getUser(compactJws : String) : User {
        val jwtParser = Jwts.parser().verifyWith(secretKey)
            .build()
        val claims = jwtParser.parseSignedClaims(compactJws).payload;
        val authorities = (claims.get("authorities") as List<Map<String,String>>).filter { it.containsKey("authority") }.map { SimpleGrantedAuthority(it.get("authority")) }
        return User(claims.id, claims.subject, authorities)
    }

    fun getToken(user : User) : Token {
        val strTokne = Jwts.builder()
            .id(user.id)
            .subject(user.name)
            .claim("authorities",user.authorities)
            .signWith(secretKey)
            .compact();
        return Token(strTokne)
    }
}

class Token(val token : String)