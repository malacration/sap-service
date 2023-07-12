package br.andrew.sap.infrastructure.configurations.uzzipay

import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.builder.RequestPixDueDateSemContaBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "uzzipay", ignoreInvalidFields = true, ignoreUnknownFields = true)
class UzziPayEnvrioment {
    lateinit var host : String
    lateinit var consultaHost : String
    var contas : List<ContaUzziPayPix> = listOf()
        set(value) {
            field = value
            if(field.isNotEmpty())
                RequestPixDueDateSemContaBuilder.setUzziPayEnvrioment(this)
        }
}


