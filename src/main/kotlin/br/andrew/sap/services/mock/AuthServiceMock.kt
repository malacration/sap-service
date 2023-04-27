package br.andrew.sap.services.mock

import br.andrew.sap.model.Login
import br.andrew.sap.model.Session
import br.andrew.sap.services.AuthService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
@Profile("test")
class AuthServiceMock : AuthService("", RestTemplate()) {


    override fun getToken(login: Login): Session {
        return Session("","",0)
    }
}