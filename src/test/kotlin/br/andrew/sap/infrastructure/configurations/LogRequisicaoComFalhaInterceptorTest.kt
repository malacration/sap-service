package br.andrew.sap.infrastructure.configurations

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse
import org.springframework.mock.http.client.MockClientHttpResponse
import java.net.URI

/**
 * O interceptor vive no RestTemplate COMPARTILHADO: o mesmo do AuthService.login (usuario e senha
 * do SAP) e das integracoes de RD Station, UzziPay, Serasa e BankPlus (client secret, refresh
 * token, senha). Logar todo corpo que falha transformava uma falha de autenticacao rotineira em
 * credencial reutilizavel gravada no log.
 */
class LogRequisicaoComFalhaInterceptorTest {

    private val interceptor = LogRequisicaoComFalhaInterceptor()

    private fun executa(url: String, corpo: String, status: HttpStatus = HttpStatus.BAD_REQUEST): String {
        val request = mock<HttpRequest>().also {
            whenever(it.uri).doReturn(URI(url))
            whenever(it.method).doReturn(HttpMethod.POST)
        }
        val execution = mock<ClientHttpRequestExecution>().also {
            whenever(it.execute(any(), any())).doReturn(
                MockClientHttpResponse(ByteArray(0), status) as ClientHttpResponse)
        }
        return capturaLog { interceptor.intercept(request, corpo.toByteArray(), execution) }
    }

    /** Captura o que foi para o logger via appender em memoria do logback. */
    private fun capturaLog(acao: () -> Unit): String {
        val logger = org.slf4j.LoggerFactory.getLogger(LogRequisicaoComFalhaInterceptor::class.java)
            as ch.qos.logback.classic.Logger
        val appender = ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>()
        appender.start()
        logger.addAppender(appender)
        try {
            acao()
        } finally {
            logger.detachAppender(appender)
        }
        return appender.list.joinToString("\n") { it.formattedMessage }
    }

    @Test
    fun `nao loga o corpo do login do SAP`() {
        val log = executa("https://sap:50000/b1s/v1/Login",
            """{"CompanyDB":"HMG","UserName":"manager","Password":"segredo"}""")

        assertFalse(log.contains("segredo"), log)
        assertFalse(log.contains("manager"), log)
        //a falha continua registrada, so sem o corpo
        assertTrue(log.contains("/b1s/v1/Login"), log)
    }

    @Test
    fun `nao loga o corpo de integracao de terceiro`() {
        val log = executa("https://api.rdstation.com/auth/token",
            """{"client_id":"abc","client_secret":"segredo","refresh_token":"rt"}""")

        assertFalse(log.contains("segredo"), log)
        assertFalse(log.contains("rt"), log)
    }

    /** O caso que motivou o interceptor: PATCH de documento no Service Layer. */
    @Test
    fun `loga o corpo de documento do service layer`() {
        val log = executa("https://sap:50000/b1s/v1/Orders(118927)",
            """{"CardCode":"CLI0003130","DocumentLines":[]}""")

        assertTrue(log.contains("CLI0003130"), log)
    }

    /** Ate no que passa, campo sensivel e mascarado: /b1s/v1/Users tem senha. */
    @Test
    fun `mascara campo sensivel dentro do service layer`() {
        val log = executa("https://sap:50000/b1s/v1/Users(1)",
            """{"UserCode":"ana","Password":"segredo","U_token":"abc123"}""")

        assertFalse(log.contains("segredo"), log)
        assertFalse(log.contains("abc123"), log)
        assertTrue(log.contains("ana"), log)
        assertTrue(log.contains("***"), log)
    }

    @Test
    fun `nao loga nada quando a resposta e 2xx`() {
        val log = executa("https://sap:50000/b1s/v1/Orders", """{"CardCode":"CLI001"}""", HttpStatus.OK)

        assertTrue(log.isEmpty(), log)
    }
}
