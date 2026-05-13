package br.andrew.sap.infrastructure.websocket

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@Component
class SessionProcessingRegistry {
    private val tokens = ConcurrentHashMap<String, MutableSet<AtomicBoolean>>()

    fun register(sessionId: String): AtomicBoolean {
        val token = AtomicBoolean(false)
        val set = tokens.computeIfAbsent(sessionId) { ConcurrentHashMap.newKeySet() }
        set.add(token)
        return token
    }

    fun cancelSession(sessionId: String) {
        tokens[sessionId]?.forEach { it.set(true) }
    }

    fun clear(sessionId: String, token: AtomicBoolean) {
        val set = tokens[sessionId] ?: return
        set.remove(token)
        if (set.isEmpty()) {
            tokens.remove(sessionId)
        }
    }

    fun clearAll(sessionId: String) {
        tokens.remove(sessionId)
    }
}
