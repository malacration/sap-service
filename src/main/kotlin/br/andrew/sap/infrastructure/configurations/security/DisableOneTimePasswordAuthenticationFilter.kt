package br.andrew.sap.infrastructure.configurations.security

import br.andrew.sap.infrastructure.configurations.security.jwt.JwtHandler
import br.andrew.sap.infrastructure.configurations.security.otp.OneTimePasswordAuthenticationSuccessHandler
import br.andrew.sap.infrastructure.configurations.security.otp.OtpCpfCnpj
import br.andrew.sap.services.OneTimePasswordService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.util.stream.Collectors


class DisableOneTimePasswordAuthenticationFilter(authManager: AuthenticationManager,
                                          jwtHandler: JwtHandler) : AbstractAuthenticationProcessingFilter(
        AntPathRequestMatcher(
        "/otp/login",
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
        val user = mapper.readValue(json, OtpCpfCnpj::class.java).getUser()
        return getAuthenticationManager().authenticate(user)
    }
}