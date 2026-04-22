package br.andrew.sap.services

import br.andrew.sap.model.Login
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.Date
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
open class AuthService(
    private val env: SapEnvrioment,
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)
    private val lock = ReentrantLock()
    @Volatile
    private var session : Session? = null

    fun getToken(login : Login) : Session {
        return ensureSession(login)
    }

    fun keepAlive(login: Login = env.getLogin()): Session {
        return ensureSession(login)
    }

    fun refreshToken(login: Login = env.getLogin()): Session {
        return ensureSession(login, forceRefresh = true)
    }

    fun invalidateSession(sessionId: String? = null) {
        lock.withLock {
            if (sessionId == null || session?.sessionId == sessionId) {
                session = null
            }
        }
    }

    fun <T> executeWithValidSession(login: Login = env.getLogin(), action: (Session) -> T): T {
        val current = ensureSession(login)
        return try {
            action(current)
        } catch (ex: HttpClientErrorException) {
            if (!isSessionInvalid(ex)) {
                throw ex
            }

            logger.warn("Sessao do Service Layer invalida, renovando login")
            invalidateSession(current.sessionId)
            val refreshed = refreshToken(login)
            action(refreshed)
        }
    }

    private fun ensureSession(login: Login, forceRefresh: Boolean = false): Session {
        val current = session
        if (!forceRefresh && current != null && !shouldRefresh(current)) {
            return current
        }

        return lock.withLock {
            val lockedSession = session
            if (!forceRefresh && lockedSession != null && !shouldRefresh(lockedSession)) {
                return lockedSession
            }

            val renewedSession = login(login)
            session = renewedSession
            renewedSession
        }
    }

    private fun login(login: Login): Session {
        val url = URI("${env.host}/b1s/v1/Login")
        return restTemplate.postForEntity(url, login, Session::class.java).body
            ?: throw Exception("Nao foi possivel obter sessao no Service Layer")
    }

    private fun shouldRefresh(session: Session): Boolean {
        if (session.isExpire()) {
            return true
        }

        val timeoutInMillis = session.sessionTimeout * 60 * 1000L
        val elapsed = Date().time - session.date.time
        val refreshWindow = calculateRefreshWindow(timeoutInMillis)
        return elapsed >= timeoutInMillis - refreshWindow
    }

    private fun calculateRefreshWindow(timeoutInMillis: Long): Long {
        val twoMinutes = 2 * 60 * 1000L
        val halfTimeout = timeoutInMillis / 2
        return minOf(twoMinutes, maxOf(5_000L, halfTimeout))
    }

    private fun isSessionInvalid(ex: HttpClientErrorException): Boolean {
        val status = ex.statusCode.value()
        return status == 401 || status == 403
    }
}
