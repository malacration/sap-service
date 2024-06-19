package br.andrew.sap.services.security

import org.springframework.security.core.AuthenticationException

class PasswordEmptyException() : AuthenticationException("Instruções de acesso foram enviadas ao seu e-mail."){

}