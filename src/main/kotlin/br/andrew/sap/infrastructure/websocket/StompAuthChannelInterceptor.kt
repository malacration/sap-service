package br.andrew.sap.infrastructure.websocket

import br.andrew.sap.infrastructure.security.RoleBasedAuthorizationFilter
import br.andrew.sap.infrastructure.security.jwt.JwtAuthenticationFilter
import br.andrew.sap.infrastructure.security.jwt.JwtHandler
import br.andrew.sap.infrastructure.security.jwt.JwtSecretBean
import br.andrew.sap.infrastructure.security.keycloak.KeycloakJwtService
import br.andrew.sap.infrastructure.security.keycloak.KeycloakProperties
import br.andrew.sap.infrastructure.security.keycloak.KeycloakUserMapper
import br.andrew.sap.infrastructure.security.keycloak.KeycloakUserProvisioningException
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.security.interfaces.RuleService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import java.security.Principal

/**
 * Autenticacao e autorizacao do canal STOMP (/ws).
 *
 * O handshake HTTP do /ws e publico (SecurityWebConf) porque o SockJS nao deixa o navegador
 * mandar o header Authorization. O token vem no frame CONNECT (connectHeaders do front) e e
 * validado aqui, com as mesmas regras dos filtros HTTP: Keycloak (RS256), token interno (HS*)
 * ou o usuario do bypass quando spring.security.disable=true.
 *
 * Sem isso o @MessageMapping recebia Principal nulo e o processamento morria em silencio - a tela
 * ficava esperando a resposta para sempre.
 *
 * - CONNECT sem token valido e recusado (o cliente recebe um frame ERROR).
 * - SEND passa pelo rules.yml como se fosse um GET HTTP no destino: quem nao pode chamar
 *   GET /calculadora-preco/... tambem nao dispara /calculadora-preco/get-async.
 * - SUBSCRIBE so em /user/...: as filas de outra sessao nao podem ser assinadas direto.
 */
@Component
class StompAuthChannelInterceptor(
    @Value("\${spring.security.disable:false}") private val disable: Boolean,
    jwtSecretBean: JwtSecretBean,
    private val keycloakProperties: KeycloakProperties,
    private val keycloakJwtService: KeycloakJwtService,
    private val keycloakUserMapper: KeycloakUserMapper,
    ruleService: RuleService,
) : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)
    private val jwtHandler = JwtHandler(jwtSecretBean)
    //destinos STOMP nao tem context-path
    private val autorizacao = RoleBasedAuthorizationFilter(ruleService, "")

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java) ?: return message
        when (accessor.command) {
            StompCommand.CONNECT -> accessor.user = WsPrincipal(autentica(accessor.getFirstNativeHeader("Authorization")))
            StompCommand.SEND -> {
                val user = usuario(accessor)
                val destino = accessor.destination ?: throw AccessDeniedException("Destino nao informado")
                if (!autorizacao.isAuthorized(destino, "GET", user))
                    throw AccessDeniedException("Você não tem permissão para acessar este recurso.")
            }
            StompCommand.SUBSCRIBE -> {
                usuario(accessor)
                if (accessor.destination?.startsWith("/user/") != true)
                    throw AccessDeniedException("So e permitido assinar filas do proprio usuario (/user/...)")
            }
            else -> {}
        }
        return message
    }

    fun autentica(header: String?): User {
        if (disable)
            return JwtAuthenticationFilter.usuarioBypass()
        val token = header?.removePrefix("Bearer ")?.trim()
        if (token.isNullOrEmpty())
            throw AccessDeniedException("Token nao informado na conexao do websocket")
        try {
            if (keycloakProperties.enabled && keycloakJwtService.looksLikeKeycloakToken(token))
                return keycloakUserMapper.toUser(keycloakJwtService.validate(token))
            if (JwtAuthenticationFilter.isInternalToken(token))
                return jwtHandler.getUser(token)
        } catch (e: KeycloakUserProvisioningException) {
            throw AccessDeniedException(e.message)
        } catch (e: Exception) {
            log.warn("Token recusado na conexao do websocket: ${e.message}")
            throw AccessDeniedException("Token invalido ou expirado")
        }
        throw AccessDeniedException("Token nao reconhecido")
    }

    private fun usuario(accessor: StompHeaderAccessor): User =
        (accessor.user as? WsPrincipal)?.user ?: throw AccessDeniedException("Conexao do websocket nao autenticada")
}

/**
 * Principal da sessao STOMP. O nome e a chave do SimpUserRegistry e do convertAndSendToUser, por
 * isso usa origem + id: User.getName() e o nome de exibicao, que pode repetir ou ser nulo.
 */
class WsPrincipal(val user: User) : Principal {
    override fun getName(): String = "${user.origin}:${user.id}"
}
