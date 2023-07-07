package br.andrew.sap.infrastructure.configurations.uzzipay

import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "uzzipay", ignoreInvalidFields = true, ignoreUnknownFields = true)
class UzziPayEnvrioment(
    ){
    lateinit var host : String
    lateinit var contas : List<ContaUzziPayPix>
}


