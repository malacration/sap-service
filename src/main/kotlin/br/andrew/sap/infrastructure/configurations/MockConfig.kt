package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.model.User
import br.andrew.sap.services.OrdersService
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate


@Profile("test")
@Configuration
class MockConfig {

    @Bean(name = arrayOf("user.current"))
    fun getCurrentUser() : User {
        return User(666)
    }
}