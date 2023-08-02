package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.model.telegram.TipoMensagem
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class TelegramConfig(@Value("\${telegram.apiUrl:}") var apiUrl: String,
                     @Value("\${telegram.token:}") val token: String,
                     @Value("\${telegram.channel:}") val chatId: String,
                     @Value("#{\${telegram.topicos:{'testes': 44}}}") val topicos: Map<String,Int>) {

    internal val messageUrl: String = "${apiUrl.removeSuffix("/")}/bot$token/sendMessage"

    init {
        TipoMensagem.topics.putAll(topicos)
    }
}