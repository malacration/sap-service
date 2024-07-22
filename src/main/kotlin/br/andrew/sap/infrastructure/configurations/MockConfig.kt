package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.model.SapUser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.context.annotation.Profile


@Profile("test || aws")
@Configuration
class MockConfig {

    @Bean(name = arrayOf("user.current"))
    fun getCurrentUser() : SapUser {
        return SapUser(666)
    }
}