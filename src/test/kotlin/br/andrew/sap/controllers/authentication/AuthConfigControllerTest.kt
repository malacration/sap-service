package br.andrew.sap.controllers.authentication

import br.andrew.sap.infrastructure.security.keycloak.KeycloakProperties
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthConfigControllerTest {

    @Test
    fun `expoe vendas offline habilitadas na configuracao publica`() {
        val response = AuthConfigController(KeycloakProperties(), true).config()

        assertTrue(response.offlineEnabled)
    }

    @Test
    fun `expoe vendas offline desabilitadas na configuracao publica`() {
        val response = AuthConfigController(KeycloakProperties(), false).config()

        assertFalse(response.offlineEnabled)
    }
}
