package br.andrew.sap.infrastructure.security.password

import br.andrew.sap.infrastructure.security.jwt.JwtHandler
import br.andrew.sap.infrastructure.security.otp.OneTimePasswordAuthenticationSuccessHandler
import br.andrew.sap.model.authentication.UserPassword
import br.andrew.sap.services.security.UserPasswordService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.util.stream.Collectors

class UserPasswordAuthenticationFilter(authManager: AuthenticationManager,
                                       jwtHandler: JwtHandler,
                                       val service : UserPasswordService
) : AbstractAuthenticationProcessingFilter(
    AntPathRequestMatcher(
        "/login",
        "POST"
    )
) {
    init {
        this.authenticationManager = authManager
        this.setAuthenticationSuccessHandler(OneTimePasswordAuthenticationSuccessHandler(jwtHandler))
    }

    override fun attemptAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Authentication? {
        val json = request!!.reader.lines().collect(Collectors.joining(System.lineSeparator()))
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val userPassword = mapper.readValue(json, UserPassword::class.java)
        return getAuthenticationManager().authenticate(service.login(userPassword))
    }
}