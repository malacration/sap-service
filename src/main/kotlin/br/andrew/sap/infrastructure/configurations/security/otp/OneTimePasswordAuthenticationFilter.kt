package br.andrew.sap.infrastructure.configurations.security.otp

import br.andrew.sap.infrastructure.configurations.security.jwt.JwtHandler
import br.andrew.sap.services.security.OneTimePasswordService
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
        this.setAuthenticationSuccessHandler(OneTimePasswordAuthenticationSuccessHandler(jwtHandler))
    }

    override fun attemptAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Authentication? {
        val json = request!!.reader.lines().collect(Collectors.joining(System.lineSeparator()))
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val user = mapper.readValue(json,OtpCpfCnpj::class.java).getUser()
        if(!otpService.checkPassword(user))
            throw Exception("Erro ao validar OTP")
        return getAuthenticationManager().authenticate(user)
    }
}

class OtpCpfCnpj(val cpfCnpj : String, val otp : Int){
    fun getUser() : User{
        return User(cpfCnpj,"Cliente", listOf()).also { it.otp = otp }
    }
}

class User(val id : String,
           private val name : String,
           private val authorities : List<SimpleGrantedAuthority>) : Authentication{

    private var authenticated = true
    var otp : Int? = null
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