package br.andrew.sap.services

import br.andrew.sap.infrastructure.configurations.TelegramConfig
import br.andrew.sap.model.telegram.Mensagem
import br.andrew.sap.model.telegram.TipoMensagem
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
        send(mensagem,TipoMensagem.geral)
    }

    fun send(mensagem: String, tipo : TipoMensagem) {
        try{
            if(config.token.isNotEmpty())
                template.postForEntity(config.messageUrl, Mensagem(config.chatId,mensagem,tipo),String::class.java)
        }catch (t : Throwable){
            logger.error("Erro ao enviar mensagem ao telegram!",t)
        }
    }

}

