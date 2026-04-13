//package br.andrew.sap.services
//
//import br.andrew.sap.model.Login
//import br.andrew.sap.model.envrioments.SapEnvrioment
//import br.andrew.sap.model.sap.Session
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Test
//import org.mockito.kotlin.any
//import org.mockito.kotlin.eq
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.times
//import org.mockito.kotlin.verify
//import org.mockito.kotlin.whenever
//import org.springframework.http.HttpHeaders
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//import org.springframework.web.client.HttpClientErrorException
//import org.springframework.web.client.RestTemplate
//import java.net.URI
//
//class AuthServiceTests {
//
//    @Test
//    fun getTokenDeveReaproveitarSessaoValida() {
//        val restTemplate = mock<RestTemplate>()
//        val env = SapEnvrioment("https://sap.local", "manager", "senha", "SBODEMO")
//        val session = Session("sessao-1", "10.0", 30)
//        whenever(
//            restTemplate.postForEntity(
//                any<URI>(),
//                any<Login>(),
//                eq(Session::class.java)
//            )
//        ).thenReturn(ResponseEntity.ok(session))
//
//        val authService = AuthService(env, restTemplate)
//
//        val first = authService.getToken(env.getLogin())
//        val second = authService.getToken(env.getLogin())
//
//        assertEquals("sessao-1", first.sessionId)
//        assertEquals("sessao-1", second.sessionId)
//        verify(restTemplate, times(1)).postForEntity(any<URI>(), any<Login>(), eq(Session::class.java))
//    }
//
//    @Test
//    fun executeWithValidSessionDeveRefazerLoginQuandoSessaoForRecusada() {
//        val restTemplate = mock<RestTemplate>()
//        val env = SapEnvrioment("https://sap.local", "manager", "senha", "SBODEMO")
//        whenever(
//            restTemplate.postForEntity(
//                any<URI>(),
//                any<Login>(),
//                eq(Session::class.java)
//            )
//        ).thenReturn(
//            ResponseEntity.ok(Session("sessao-antiga", "10.0", 30)),
//            ResponseEntity.ok(Session("sessao-nova", "10.0", 30))
//        )
//
//        val authService = AuthService(env, restTemplate)
//        var attempts = 0
//        val result = authService.executeWithValidSession(env.getLogin()) { session ->
//            attempts++
//            if (attempts == 1) {
//                throw HttpClientErrorException.create(
//                    HttpStatus.UNAUTHORIZED,
//                    "Unauthorized",
//                    HttpHeaders.EMPTY,
//                    ByteArray(0),
//                    null
//                )
//            }
//            session.sessionId
//        }
//
//        assertEquals("sessao-nova", result)
//        assertEquals(2, attempts)
//        verify(restTemplate, times(2)).postForEntity(any<URI>(), any<Login>(), eq(Session::class.java))
//    }
//}
