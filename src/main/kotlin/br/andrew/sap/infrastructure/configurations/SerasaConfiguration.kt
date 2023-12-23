package br.andrew.sap.infrastructure.configurations

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


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

class SerasaProperties(val url : String, val user : String,
                       val password: String) {
    fun getBase(): String {
        val newPassword = "        "
        "https://mqlinuxext-2.serasa.com.br/Homologa/consultahttps?p=94382992rovema@1"
        val parametros = "        B49C      082639170000185JC     FI                   S99SINIAN                               N                                                                                                                                                                                                                                                                                                                  P002RSPU                                                                                                           I00100R                                                                                                            T999"
        return "$url/consultahttps?p=$user${password}${newPassword}"
    }
}