package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.model.telegram.TipoMensagem
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class TelegramConfig(@Value("\${telegram.plantao.apiUrl:}") var apiUrl: String,
                     @Value("\${telegram.plantao.token:}") val token: String,
                     @Value("\${telegram.plantao.channel:}") val chatId: String,
                     @Value("\${telegram.plantao.topicos:}") val topicos: Map<String,Int>) {

    internal val messageUrl: String = "${apiUrl.removeSuffix("/")}/bot$token/sendMessage"

    init {
        TipoMensagem.topics.putAll(topicos)
    }
}