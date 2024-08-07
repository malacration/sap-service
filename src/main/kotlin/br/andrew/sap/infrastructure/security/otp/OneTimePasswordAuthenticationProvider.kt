package br.andrew.sap.infrastructure.security.otp

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder


class OneTimePasswordAuthenticationProvider : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        SecurityContextHolder.getContext().authentication = authentication
        return authentication
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return true
    }
}