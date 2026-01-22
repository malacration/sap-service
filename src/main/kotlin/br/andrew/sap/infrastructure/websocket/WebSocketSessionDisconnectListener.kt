package br.andrew.sap.infrastructure.websocket

import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketSessionDisconnectListener(
    private val sessionRegistry: SessionProcessingRegistry
) {
    @EventListener
    fun handleSessionConnect(event: SessionConnectEvent) {
        val sessionId = StompHeaderAccessor.wrap(event.message).sessionId ?: return
        sessionRegistry.register(sessionId)
    }

    @EventListener
    fun handleSessionDisconnect(event: SessionDisconnectEvent) {
        val sessionId = event.sessionId ?: return
        sessionRegistry.cancelSession(sessionId)
        sessionRegistry.clearAll(sessionId)
    }
}
