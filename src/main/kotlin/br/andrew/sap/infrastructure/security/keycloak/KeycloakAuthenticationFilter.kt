package br.andrew.sap.infrastructure.security.keycloak

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filtro que autentica requisicoes portando um access token do Keycloak.
 *
 * Roda antes do [br.andrew.sap.infrastructure.security.jwt.JwtAuthenticationFilter].
 * So age quando o Keycloak esta habilitado e o token tem cara de Keycloak
 * (issuer do realm). Tokens internos (ex.: emitidos pelo `/otp/login`) sao
 * ignorados aqui e seguem para o filtro interno, mantendo a autenticacao de
 * cliente externo funcionando.
 */
class KeycloakAuthenticationFilter(
    private val properties: KeycloakProperties,
    private val keycloakJwtService: KeycloakJwtService,
    private val userMapper: KeycloakUserMapper,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(KeycloakAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!properties.enabled || SecurityContextHolder.getContext().authentication != null) {
            filterChain.doFilter(request, response)
            return
        }

        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")?.trim()
        if (!token.isNullOrEmpty() && keycloakJwtService.looksLikeKeycloakToken(token)) {
            try {
                val claims = keycloakJwtService.validate(token)
                SecurityContextHolder.getContext().authentication = userMapper.toUser(claims)
            } catch (e: KeycloakUserProvisioningException) {
                // Provisionamento incompleto no SSO: nao autentica e registra a
                // mensagem. Rotas publicas (ex.: faturas) seguem acessiveis;
                // rotas protegidas serao barradas pelo KeycloakProvisioningEntryPoint
                // exibindo esta mensagem.
                log.warn("Acesso via SSO incompleto: ${e.message}")
                SecurityContextHolder.clearContext()
                request.setAttribute(PROVISIONING_ERROR_ATTR, e.message)
            } catch (e: Exception) {
                log.error("Falha ao autenticar token Keycloak", e)
            }
        }
        filterChain.doFilter(request, response)
    }
}
