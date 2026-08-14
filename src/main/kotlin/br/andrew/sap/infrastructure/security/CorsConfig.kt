package br.andrew.sap.infrastructure.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*


//corsAppendAllow nao tem valor default de proposito - assim ninguem consegue
//instanciar CorsConfig() na mao e perder silenciosamente o que veio do ambiente
@Configuration
class CorsConfig(@Value("\${cors.origins:http://localhost:4200}") corsAppendAllow : List<String>) {

    //allowedOriginPatterns e nao allowedOrigins: allowedOrigins e comparacao
    //exata, entao "[*]" e "*" entrariam como texto literal e nunca casariam
    val allowedOriginPatterns = mutableListOf(
        "http://localhost:[*]",
        "http://*localhost:[*]",
        "http://172.18.30.147:[*]"
    ).also {
        it.addAll(corsAppendAllow.map(::normalizeOrigin).filter(String::isNotEmpty))
    }

    val allowedMethods = mutableListOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

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
        configuration.allowedOriginPatterns = allowedOriginPatterns
        configuration.allowedMethods = allowedMethods
        configuration.allowedHeaders = allowedHeaders
        configuration.exposedHeaders = exposedHeaders
        configuration.allowCredentials = true
        return configuration
    }

    //o corpo do lambda anterior declarava uma funcao aninhada "customize" e nunca
    //a chamava - o Customizer real ficava vazio e o Spring Security nunca aplicava
    //essa configuracao (o CORS acabava caindo no default fraco do WebConfig)
    var customizer : Customizer<CorsConfigurer<HttpSecurity>> = Customizer<CorsConfigurer<HttpSecurity>> { httpSecurityCorsConfigurer ->
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", getCorsConfig())
        httpSecurityCorsConfigurer.configurationSource(source)
    }

    companion object {
        //o header Origin so tem esquema/host/porta - sufixo de path ("/", "/*",
        //"/**") nunca casa. as aspas aparecem quando a env var e declarada em
        //docker-compose/k8s no formato cors.origins="http://a","http://b", que
        //entrega as aspas dentro de cada item da lista
        fun normalizeOrigin(origin : String) = origin.trim()
            .removeSurrounding("\"")
            .trim()
            .removeSuffix("/**")
            .removeSuffix("/*")
            .removeSuffix("/")
    }
}
