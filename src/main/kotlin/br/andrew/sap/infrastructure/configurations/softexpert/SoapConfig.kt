package br.andrew.sap.infrastructure.configurations.softexpert

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SoapConfig(val envrioment : SoftExpertEnvrioment) {

    @Bean
    fun soapConnector(): SOAPConnector {
        val client = SOAPConnector(envrioment)
        RequestTest()
        return client
    }
}

