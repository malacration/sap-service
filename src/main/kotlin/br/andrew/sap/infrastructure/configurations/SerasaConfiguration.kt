package br.andrew.sap.infrastructure.configurations

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource


@Configuration
@ConfigurationProperties(prefix = "serasa")
class SerasaConfiguration() {

    var url : String = "";
    var user : String = "";
    var password : String = "";

    @Bean
    fun serasaProperties() : SerasaProperties {
        return SerasaProperties(url, user, password)
    }

}

// serasaProperties
class SerasaProperties(val url : String, val user : String,
                       val password: String) {
    fun getBase(): String {
        return "$url/consultahttps?p=$user${password}B49C 000000000000000FC FI S99SINIAN N P002RSPU I00100R T999"

    }
}
