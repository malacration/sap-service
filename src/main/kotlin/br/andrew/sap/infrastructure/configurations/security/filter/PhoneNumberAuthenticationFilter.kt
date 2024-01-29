package br.andrew.sap.infrastructure.configurations.security.filter

import br.andrew.sap.model.bank.Payment
import br.andrew.sap.services.OneTimePasswordService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import java.util.stream.Collectors


class PhoneNumberAuthenticationFilter(authManager: AuthenticationManager,
                                      val otpService : OneTimePasswordService) : AbstractAuthenticationProcessingFilter(
        AntPathRequestMatcher(
        "/otp/login",
        "POST"
    )
) {
    init {
        this.authenticationManager = authManager
        this.setAuthenticationSuccessHandler(PhoneNumberAuthenticationSuccessHandler())
    }

    override fun attemptAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Authentication? {
        val auth = User("andrew","andrew nome", listOf<SimpleGrantedAuthority>())
        if ("POST" == request?.method) {
            val json = request.reader.lines().collect(Collectors.joining(System.lineSeparator()))
            val mapper = ObjectMapper().registerModule(KotlinModule())
            val otpData = mapper.readValue<OtpCpfCnpj>(json,OtpCpfCnpj::class.java)
            return getAuthenticationManager().authenticate(auth)
        }
        return getAuthenticationManager().authenticate(auth)
    }
}

class OtpCpfCnpj(val cpfCnpj : String, val otp : String)

class User(val id : String,
           private val name : String,
           private val authorities : List<SimpleGrantedAuthority>) : Authentication{

    private var authenticated = true
    override fun getName(): String {
         return "Andrew"
    }

    override fun getAuthorities(): List<out GrantedAuthority> {
        return authorities
    }

    override fun getCredentials(): Any {
        return this
    }

    override fun getDetails(): Any {
        return this
    }

    override fun getPrincipal(): Any {
        return this
    }

    override fun isAuthenticated(): Boolean {
        return authenticated
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }

}