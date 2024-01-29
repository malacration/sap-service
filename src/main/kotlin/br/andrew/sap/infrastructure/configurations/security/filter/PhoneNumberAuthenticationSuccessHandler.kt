package br.andrew.sap.infrastructure.configurations.security.filter

import io.jsonwebtoken.Jwts
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler


class PhoneNumberAuthenticationSuccessHandler : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        user: Authentication
    ) {
        if(user is User) {
            val key = Jwts.SIG.HS256.key().build()
            val saida = Jwts.builder().id(user.id).subject(user.name).signWith(key).compact();
            //response.contentType = "application/json"
            //response.writer?.println("token : '$saida'")
            response.writer?.print(saida)
        }
    }

}
