package br.andrew.sap.services

import br.andrew.sap.model.Login
import br.andrew.sap.model.Session
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Caching
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
@Profile("!test")
open class AuthService(
        private @Value("\${sap.service.layer.url:https://localhost:50000}") val host : String,
        private val restTemplate: RestTemplate) {

    companion object{
        private var session :Session? = null
    }
    @Caching
    fun getToken(login : Login) : Session {
        val url = URI("$host/b1s/v1/Login")
        if(session == null || session!!.isExpire())
            session = restTemplate.postForEntity(url,login, Session::class.java).body
        return session!!;
    }
}