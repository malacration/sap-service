package br.andrew.sap.infrastructure.security.keycloak

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint

/** Atributo da request onde o filtro guarda o motivo do provisionamento SSO incompleto. */
const val PROVISIONING_ERROR_ATTR = "keycloak_provisioning_error"

/**
 * Entry point usado quando uma requisicao NAO autenticada cai numa rota
 * protegida. Se o [KeycloakAuthenticationFilter] marcou um problema de
 * provisionamento (sem codigo de colaborador / sem role), responde 403 com a
 * mensagem explicativa; caso contrario delega ao comportamento padrao (403).
 *
 * Assim, rotas publicas continuam acessiveis mesmo com um token SSO
 * mal-provisionado presente, e so as rotas protegidas exibem o erro.
 */
class KeycloakProvisioningEntryPoint(
    private val delegate: AuthenticationEntryPoint = Http403ForbiddenEntryPoint(),
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException?,
    ) {
        val message = request.getAttribute(PROVISIONING_ERROR_ATTR) as? String
        if (message != null && !response.isCommitted) {
            response.status = HttpServletResponse.SC_FORBIDDEN
            response.contentType = "text/plain;charset=UTF-8"
            response.writer.write(message)
            response.writer.flush()
        } else {
            delegate.commence(request, response, authException)
        }
    }
}
