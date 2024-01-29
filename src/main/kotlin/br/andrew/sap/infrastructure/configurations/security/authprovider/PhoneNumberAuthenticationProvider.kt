package br.andrew.sap.infrastructure.configurations.security.authprovider

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder


class PhoneNumberAuthenticationProvider : AuthenticationProvider {

    override fun authenticate(authentication: Authentication): Authentication {
        SecurityContextHolder.getContext().authentication = authentication
        return authentication
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return true
    }
}