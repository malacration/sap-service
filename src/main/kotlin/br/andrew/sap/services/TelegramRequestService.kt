package br.andrew.sap.services

import br.andrew.sap.infrastructure.configurations.TelegramConfig
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestClientResponseException
import java.nio.charset.StandardCharsets

@Component
class TelegramRequestService(val config: TelegramConfig, val template: RestTemplate) {
    init {
        template.messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
    }

    val logger: Logger = LoggerFactory.getLogger(TelegramRequestService::class.java)

    fun send(mensagem: String) {
        template.getForEntity(config.messageUrl+"&text=$mensagem",String::class.java)
    }

}

