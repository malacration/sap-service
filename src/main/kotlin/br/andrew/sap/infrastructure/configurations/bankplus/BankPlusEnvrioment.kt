package br.andrew.sap.infrastructure.configurations.bankplus

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "bankplus", ignoreInvalidFields = true, ignoreUnknownFields = true)
class BankPlusEnvrioment {
    lateinit var host : String
    lateinit var token : String
}


