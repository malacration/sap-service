package br.andrew.sap.infrastructure.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*


@Configuration
class CorsConfig(@Value("\${cors.origins:http://localhost:4200}") val corsAppendAllow : List<String> = arrayListOf()) {

    val allowedOrigins = mutableListOf(
        "http://localhost:[*]",
        "http://localhost:4200/",
        "http://localhost:4200",
        "http://*localhost:[*]",
        "http://172.18.30.147:4200/*",
        "http://172.18.30.147:4200",
        "http://172.18.30.147:4200/"
    ).also {
        it.addAll(corsAppendAllow)
    }

    val allowedMethods = mutableListOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")

    val allowedHeaders = mutableListOf("Authorization",
        "Cache-Control",
        "Content-Type",
        "cache",
        "pragma",
        "traceparent",
        "tracestate")

    val exposedHeaders = mutableListOf(
        "Authorization",
        "error",
        "arquivo",
        "info",
        "cache",
        "Content-Type")

    fun getCorsConfig(): CorsConfiguration {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = allowedOrigins
        configuration.allowedMethods = allowedMethods
        configuration.allowedHeaders = allowedHeaders
        configuration.exposedHeaders = exposedHeaders
        configuration.allowCredentials = true
        return configuration
    }

    var customizer : Customizer<CorsConfigurer<HttpSecurity>> = Customizer<CorsConfigurer<HttpSecurity>> {
        fun customize(httpSecurityCorsConfigurer: CorsConfigurer<HttpSecurity?>) {
            object : UrlBasedCorsConfigurationSource() {
                init {
                    val configuration: CorsConfiguration = getCorsConfig();
                    this.registerCorsConfiguration("/**", configuration.applyPermitDefaultValues())
                    this.registerCorsConfiguration("**", configuration)
                    this.registerCorsConfiguration("*", configuration)
                    this.registerCorsConfiguration("/*", configuration)
                    this.registerCorsConfiguration("**/**", configuration)
                    httpSecurityCorsConfigurer.configurationSource(this)
                }
            }
        }
    }


}