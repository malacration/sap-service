package br.andrew.sap.infrastructure.configurations.invent

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "taxplus.nfe", ignoreInvalidFields = true, ignoreUnknownFields = true)
class TaxNfeEnvrioment {
    var host : String = ""
    var token : String  = ""
    var base : String  = ""
}


