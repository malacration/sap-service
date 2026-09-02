package br.andrew.sap.controllers.authentication

import br.andrew.sap.infrastructure.security.keycloak.KeycloakProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoint publico consultado pelo front-sap para descobrir como deve
 * autenticar: modo interno (usuario/senha) ou via Keycloak (OIDC).
 */
@RestController
@RequestMapping("auth")
class AuthConfigController(
    private val keycloak: KeycloakProperties,
    @Value("\${offline.enabled:false}") private val offlineEnabled: Boolean,
) {

    @GetMapping("/config")
    fun config(): AuthConfigResponse {
        return if (keycloak.enabled) {
            AuthConfigResponse(
                mode = "keycloak",
                keycloak = KeycloakClientConfig(
                    url = keycloak.url,
                    realm = keycloak.realm,
                    clientId = keycloak.clientId,
                ),
                offlineEnabled = offlineEnabled,
            )
        } else {
            AuthConfigResponse(mode = "internal", keycloak = null, offlineEnabled = offlineEnabled)
        }
    }
}

data class AuthConfigResponse(
    val mode: String,
    val keycloak: KeycloakClientConfig?,
    val offlineEnabled: Boolean,
)

data class KeycloakClientConfig(
    val url: String,
    val realm: String,
    val clientId: String,
)
