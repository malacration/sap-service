package br.andrew.sap.infrastructure.websocket

import br.andrew.sap.infrastructure.security.jwt.JwtHandler
import br.andrew.sap.infrastructure.security.jwt.JwtSecretBean
import br.andrew.sap.infrastructure.security.keycloak.KeycloakJwtService
import br.andrew.sap.infrastructure.security.keycloak.KeycloakProperties
import br.andrew.sap.infrastructure.security.keycloak.KeycloakUserMapper
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.security.Rule
import br.andrew.sap.services.security.interfaces.RuleService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.AccessDeniedException

class StompAuthChannelInterceptorTest {

    private val secret = JwtSecretBean("segredo-de-teste-com-tamanho-suficiente-para-hmac")
    private val jwtHandler = JwtHandler(secret)
    private val channel = mock<MessageChannel>()

    private val rules = object : RuleService {
        override fun get(role: String) = when (role) {
            "admin" -> listOf(Rule("/**", "*"))
            "vendedor" -> listOf(Rule("/quotations/**", "*"))
            else -> listOf()
        }
    }

    private fun interceptor(disable: Boolean = false) = StompAuthChannelInterceptor(
        disable, secret, KeycloakProperties(), mock<KeycloakJwtService>(), mock<KeycloakUserMapper>(), rules)

    private fun token(vararg roles: String) = jwtHandler.getToken(
        User("7", "Fulano", UserOriginEnum.EmployeesInfo, "fulano", "", "", listOf(), roles.toList())).token

    private fun frame(command: StompCommand, destination: String? = null, authorization: String? = null, user: WsPrincipal? = null): Message<ByteArray> {
        val accessor = StompHeaderAccessor.create(command)
        destination?.let { accessor.destination = it }
        authorization?.let { accessor.setNativeHeader("Authorization", it) }
        user?.let { accessor.user = it }
        accessor.setLeaveMutable(true)
        return MessageBuilder.createMessage(ByteArray(0), accessor.messageHeaders)
    }

    private fun usuarioDoConnect(message: Message<*>) =
        StompHeaderAccessor.wrap(message).user as WsPrincipal

    @Test
    fun `connect com token interno autentica o usuario`() {
        val msg = frame(StompCommand.CONNECT, authorization = token("admin"))
        interceptor().preSend(msg, channel)

        val principal = usuarioDoConnect(msg)
        assertEquals("7", principal.user.id)
        assertEquals("EmployeesInfo:7", principal.name)
    }

    @Test
    fun `connect aceita o prefixo Bearer`() {
        val msg = frame(StompCommand.CONNECT, authorization = "Bearer " + token("admin"))
        interceptor().preSend(msg, channel)
        assertEquals("7", usuarioDoConnect(msg).user.id)
    }

    @Test
    fun `connect sem token e recusado`() {
        assertThrows<AccessDeniedException> { interceptor().preSend(frame(StompCommand.CONNECT), channel) }
    }

    @Test
    fun `connect com token adulterado e recusado`() {
        val adulterado = token("admin").dropLast(3) + "abc"
        assertThrows<AccessDeniedException> {
            interceptor().preSend(frame(StompCommand.CONNECT, authorization = adulterado), channel)
        }
    }

    @Test
    fun `modo disable conecta como o usuario do bypass mesmo sem token`() {
        val msg = frame(StompCommand.CONNECT)
        interceptor(disable = true).preSend(msg, channel)
        assertTrue(usuarioDoConnect(msg).user.roles.contains("admin"))
    }

    @Test
    fun `send respeita o rules yml`() {
        val admin = WsPrincipal(jwtHandler.getUser(token("admin")))
        val vendedor = WsPrincipal(jwtHandler.getUser(token("vendedor")))

        assertDoesNotThrow {
            interceptor().preSend(frame(StompCommand.SEND, "/calculadora-preco/get-async", user = admin), channel)
        }
        assertThrows<AccessDeniedException> {
            interceptor().preSend(frame(StompCommand.SEND, "/calculadora-preco/get-async", user = vendedor), channel)
        }
    }

    @Test
    fun `send sem sessao autenticada e recusado`() {
        assertThrows<AccessDeniedException> {
            interceptor().preSend(frame(StompCommand.SEND, "/calculadora-preco/get-async"), channel)
        }
    }

    @Test
    fun `subscribe so em filas do proprio usuario`() {
        val admin = WsPrincipal(jwtHandler.getUser(token("admin")))

        assertDoesNotThrow {
            interceptor().preSend(frame(StompCommand.SUBSCRIBE, "/user/queue/calculadora-preco/get-async", user = admin), channel)
        }
        //fila resolvida de outra sessao - sem a restricao daria para ler o resultado alheio
        assertThrows<AccessDeniedException> {
            interceptor().preSend(frame(StompCommand.SUBSCRIBE, "/queue/calculadora-preco/get-async-userabc123", user = admin), channel)
        }
    }
}
