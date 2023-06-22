package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.model.User
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.context.annotation.Profile


@Profile("test")
@Configuration
class MockConfig {

    @Bean(name = arrayOf("user.current"))
    fun getCurrentUser() : User {
        return User(666)
    }
}