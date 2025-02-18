package br.andrew.sap.model.envrioments

import br.andrew.sap.model.Login
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service


@Service
class SapEnvrioment(@Value("\${sap.service.layer.url:https://localhost:50000}") val host: String,
                    @Value("\${sap.service.layer.user:user}") val user: String,
                    @Value("\${sap.service.layer.password:password}") val password: String,
                    @Value("\${sap.service.layer.companyDB:companyDB}") val companyDB: String) {

    fun getLogin() : Login {
        return Login(user,password,companyDB)
    }
}