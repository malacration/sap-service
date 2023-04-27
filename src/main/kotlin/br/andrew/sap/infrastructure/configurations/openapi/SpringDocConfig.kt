package br.andrew.sap.infrastructure.configurations.openapi

import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocConfig {
    @Bean
    fun apiInfo(): OpenAPI {
        return OpenAPI()
    }

    @Bean
    fun httpApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
                .group("mock-chat-bot")
                .pathsToMatch("/mock/**")
                .build()
    }
}