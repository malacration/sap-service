package br.andrew.sap.infrastructure.security

import br.andrew.sap.controllers.BacenController
import br.andrew.sap.infrastructure.security.jwt.JwtHandler
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.security.PasswordEmptyException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler


class AuthenticationHandler(
    private val jwtHandler: JwtHandler
) : AuthenticationSuccessHandler, AuthenticationFailureHandler {

    val logger = LoggerFactory.getLogger(AuthenticationHandler::class.java)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        user: Authentication
    ) {
        response.setHeader("Access-Control-Allow-Origin","*")
        response.contentType = "text/plain"
        response.characterEncoding = "UTF-8"
        if(user is User) {
            response.writer.print(
                ObjectMapper().registerModule(KotlinModule()).writeValueAsString(jwtHandler.getToken(user))
            )
        }
        response.writer.flush()
        response.flushBuffer()
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        logger.error("onAuthenticationFailure",exception)
        response.setHeader("Access-Control-Allow-Origin","*")
        val msg = when(exception::class) {
            PasswordEmptyException()::class -> exception.message
            UsernameNotFoundException::class -> "Usuario ou Senha Invalido"
            BadCredentialsException::class -> "Usuario ou Senha Invalido"
            else -> "Usuario ou Senha Invalido"
        }

        response.writer?.print(msg)
        response.status = 500
        response.writer.flush()
        response.flushBuffer()
    }

}
