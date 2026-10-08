package br.andrew.sap.infrastructure.security.keycloak

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Configuracao do login via Keycloak.
 *
 * Funciona como um toggle: quando [enabled] for `false` o sistema continua
 * usando exclusivamente a autenticacao interna (`/logar`) e a de cliente
 * externo (`/otp/login`). Quando `true`, o backend passa a aceitar tambem os
 * tokens emitidos pelo Keycloak (RS256, validados via JWKS) e o front-sap e
 * instruido, via `GET /auth/config`, a iniciar o fluxo OIDC.
 *
 * Mesmo com o Keycloak habilitado, os tokens internos (emitidos pelo
 * `/otp/login`) continuam sendo aceitos, preservando a autenticacao de
 * cliente externo.
 */
@Configuration
@ConfigurationProperties(prefix = "keycloak")
class KeycloakProperties {
    /** Liga/desliga a autenticacao via Keycloak. */
    var enabled: Boolean = false

    /** URL base do servidor Keycloak, ex.: https://keycloak.exemplo.com */
    var url: String = ""

    /** Realm do Keycloak, ex.: rovema */
    var realm: String = ""

    /** Client id publico usado pelo front-sap (Authorization Code + PKCE). */
    var clientId: String = ""

    /**
     * Nome do claim que carrega o codigo SAP do usuario (o `EmployeeID` do
     * colaborador). E a unica fonte do [br.andrew.sap.model.authentication.User.id];
     * se estiver ausente/invalido, a autenticacao via SSO e recusada com 403.
     */
    var sapIdClaim: String = "sap_code"

    /**
     * Cliente de administracao: com [Admin.enabled], criar um perfil na tela de Regras de Acesso cria
     * tambem a role de client no Keycloak. Precisa de um client confidencial com service account
     * (client_credentials) e, em realm-management, as roles view-clients e manage-clients.
     * Desligado por padrao: sem ele a tela so avisa para criar a role la.
     */
    var admin: Admin = Admin()

    class Admin {
        var enabled: Boolean = false
        var clientId: String = ""
        var clientSecret: String = ""
        var timeoutMs: Long = 5000
    }

    /** issuer esperado nos tokens, derivado de [url] + [realm]. */
    fun issuer(): String = "${url.trimEnd('/')}/realms/$realm"

    /** Endpoint JWKS (chaves publicas) do realm. */
    fun jwksUri(): String = "${issuer()}/protocol/openid-connect/certs"
}
