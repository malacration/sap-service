package br.andrew.sap.infrastructure.security.otp

import br.andrew.sap.infrastructure.security.AuthenticationHandler
import br.andrew.sap.infrastructure.security.jwt.JwtHandler
import br.andrew.sap.model.authentication.OtpCpfCnpj
import br.andrew.sap.services.security.OneTimePasswordService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.util.stream.Collectors


class OneTimePasswordAuthenticationFilter(authManager: AuthenticationManager,
                                          jwtHandler: JwtHandler,
                                          val otpService : OneTimePasswordService
) : AbstractAuthenticationProcessingFilter(
        AntPathRequestMatcher(
        "/otp/login",
        "POST"
    )
) {
    init {
        this.authenticationManager = authManager
        this.setAuthenticationSuccessHandler(AuthenticationHandler(jwtHandler))
    }

    override fun attemptAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Authentication? {
        val json = request!!.reader.lines().collect(Collectors.joining(System.lineSeparator()))
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val user = mapper.readValue(json, OtpCpfCnpj::class.java).getUser()
        if(!otpService.checkPassword(user))
            throw Exception("Erro ao validar OTP")
        return getAuthenticationManager().authenticate(user)
    }
}