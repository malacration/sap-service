package br.andrew.sap.infrastructure.configurations.security.otp

import br.andrew.sap.infrastructure.configurations.security.jwt.JwtHandler
import br.andrew.sap.infrastructure.configurations.security.jwt.JwtSecretBean
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler


class OneTimePasswordAuthenticationSuccessHandler(
    private val jwtHandler: JwtHandler
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        user: Authentication
    ) {
        response.setHeader("Access-Control-Allow-Origin","*")
        if(user is User) {
            response.writer?.print(jwtHandler.getToken(user))
        }
    }

}
