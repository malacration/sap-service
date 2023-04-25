package br.andrew.sap.infrastructure.configurations

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

    @Bean
    @Primary
    fun nameService(): OrdersService {
        return Mockito.mock(OrdersService::class.java)
    }

    @Bean
    fun restTemplate(): RestTemplate?{
        return Mockito.mock(RestTemplate::class.java)
    }
}