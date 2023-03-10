package br.andrew.sap.rovema.services

import br.andrew.sap.rovema.model.Login
import br.andrew.sap.rovema.model.Session
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class AuthService(
        private @Value("\${sap.service.layer.url:https://localhost:50000}") val host : String,
        private val restTemplate: RestTemplate) {

    fun getToken(login : Login) : Session {
        val url = URI("$host/b1s/v1/Login")
        return restTemplate.postForEntity(url,login, Session::class.java).body!!
    }
}