package br.andrew.sap.infrastructure.security.keycloak

/**
 * Lancada durante a desserializacao do token do Keycloak quando o usuario do
 * SSO esta com o provisionamento incompleto para operar no sistema (ex.: sem
 * codigo de colaborador ou sem nenhuma permissao/role valida).
 *
 * O [KeycloakAuthenticationFilter] intercepta essa excecao e interrompe o
 * fluxo com 403 + mensagem, sem deixar a requisicao seguir.
 */
class KeycloakUserProvisioningException(message: String) : RuntimeException(message)
