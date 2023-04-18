package br.andrew.sap.infrastructure.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class TelegramConfig(@Value("\${telegram.plantao.apiUrl:}") var apiUrl: String,
                     @Value("\${telegram.plantao.token:}") val token: String,
                     @Value("\${telegram.plantao.channel:}") private val channel: String) {

    internal val messageUrl: String = "${apiUrl.removeSuffix("/")}/bot$token/sendMessage"
}