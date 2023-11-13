package br.andrew.sap.services

import br.andrew.sap.model.SalePerson
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.telegram.TipoMensagem
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
    val salesPersonsService: SalesPersonsService,
    val telegramRequestService: TelegramRequestService,
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
        val vendedor = salesPersonsService.getById(bp.salesPersonCode!!).tryGetValue<SalePerson>()
        val to = mutableListOf<String>()
        vendedor.getEmailAddress().also {
            to.add(it)
        }
        From.COMERCIAL.getFrom()?.email?.also {
            to.add(it)
        }

        MyMailMessage(to, titulo, body)
            .also { mailService.sendEmail(it,true) }
        telegramRequestService.send("${titulo} - ${link}",TipoMensagem.geral)
        return body
    }
}