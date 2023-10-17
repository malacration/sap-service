package br.andrew.sap.services

import br.andrew.sap.model.partner.BusinessPartner
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*


@Service
class AtualizacaoCadastralService(
    val mailService: MailService,
    val templateEngine : TemplateEngine,
    val bpService: BusinessPartnersService,
    @Value("\${telegram.apiUrl:}") var apiUrl: String,
    @Value("\${atualizacao.url:http://localhost:4200/cadastro}")val urlAtualizacao : String
) {

    fun atualizaCardCode(bp: BusinessPartner): String {
        val key = bp.generateKey(MessageDigest.getInstance("SHA-256"))
        val link = "$urlAtualizacao/$key"

        val body = templateEngine.process("atualizacao-cadastral",
            Context().also {
                it.setVariables(mapOf("name" to bp.cardName))
                it.setVariables(mapOf("link" to link))
            }
        )
        val data = SimpleDateFormat("dd/MM/yyy").format(Date())
        val titulo = "Atualização cadastral | ${bp.cardName} - ${data}"

        bpService.atualizaKey(key,bp)

        MyMailMessage("andrewc3po@gmail.com", titulo, body)
            .also { mailService.sendEmail(it,true) }
        return body
    }
}