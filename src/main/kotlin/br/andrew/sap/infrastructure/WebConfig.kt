package br.andrew.sap.infrastructure

import br.andrew.sap.infrastructure.security.CorsConfig
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

//sem os allowed* explicitos, o Spring aplica o default fraco do MVC (so
//GET/HEAD/POST) - por isso reaproveitamos a mesma config do CorsConfig
@Configuration
@EnableWebMvc
class WebConfig(private val corsConfig: CorsConfig) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(*corsConfig.allowedOrigins.toTypedArray())
            .allowedMethods(*corsConfig.allowedMethods.toTypedArray())
            .allowedHeaders(*corsConfig.allowedHeaders.toTypedArray())
            .exposedHeaders(*corsConfig.exposedHeaders.toTypedArray())
            .allowCredentials(true)
    }
}