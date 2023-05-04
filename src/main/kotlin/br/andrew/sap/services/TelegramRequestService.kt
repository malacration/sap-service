package br.andrew.sap.services

import br.andrew.sap.infrastructure.configurations.TelegramConfig
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.converter.StringHttpMessageConverter
import java.nio.charset.StandardCharsets

@Component
class TelegramRequestService(val config: TelegramConfig, val template: RestTemplate) {
    init {
        template.messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
    }

    val logger: Logger = LoggerFactory.getLogger(TelegramRequestService::class.java)

    fun send(mensagem: String) {
        try{
            if(config.token.isNotEmpty())
                template.postForEntity(config.messageUrl,Mensagem(config.chatId,mensagem),String::class.java)
        }catch (t : Throwable){
            logger.error("Erro ao enviar mensagem ao telegram!",t)
        }

    }
    class Mensagem(val chat_id : String, val text : String, val parse_mode : String = "html"){
        val message_thread_id : String? = if(chat_id.split("_").size > 1)
                chat_id.split("_")[1]
            else
                null
    }

}

