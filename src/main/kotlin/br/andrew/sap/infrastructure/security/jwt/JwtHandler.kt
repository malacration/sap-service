package br.andrew.sap.infrastructure.security.jwt

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import io.jsonwebtoken.Jwts
import javax.crypto.SecretKey

class JwtHandler(jwtSecret : JwtSecretBean) {

    private val secretKey : SecretKey = jwtSecret.getKey()

    fun getUser(compactJws : String) : User {
        val jwtParser = Jwts.parser().verifyWith(secretKey)
            .build()
        val claims = jwtParser.parseSignedClaims(compactJws).payload;
        val authorities = (claims.get("authorities") as List<Map<String,String>>)
            .filter { it.containsKey("authority") }
            .mapNotNull { it.get("authority") }
            .mapNotNull { runCatching { it }.getOrNull() }
        val origin = UserOriginEnum.valueOf(claims.get("origin").toString())
        val username = claims.get("username").toString()
        val emailAdress = claims.get("emailAddress").toString()
        return User(claims.id, claims.subject,origin,username,emailAdress,"none",authorities)
    }

    fun getToken(user : User) : Token {
        val strTokne = Jwts.builder()
            .id(user.id)
            .subject(user.name)
            .claim("authorities",user.authorities)
            .claim("origin",user.origin)
            .claim("username",user.username)
            .claim("emailAddress",user.emailAddress)
            .signWith(secretKey)
            .compact();
        return Token(strTokne)
    }
}

class Token(val token : String)