package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.SalePerson
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.text.SimpleDateFormat
import java.util.Date

@RestController
@RequestMapping("mail")
class MailController(val mailService: MailService,
                     val templateEngine : TemplateEngine,
                     val salesService : SalesPersonsService,
                     val telegramMsg: TelegramRequestService,
                     val service: ParcelasAbertoService) {


    val logger: Logger = LoggerFactory.getLogger(MailController::class.java)


    @GetMapping("/inadimplencia/teste/{slpCode}")
    fun testarEmail(@PathVariable slpCode : Int): String {
        val vendedor = salesService.getById(slpCode).tryGetValue<SalePerson>()
        val titulos = service.getAllBySql(vendedor.SalesEmployeeCode)
        return this.templateEngine.process("relatorio-inadiplencia",
            Context().also {
                it.setVariables(mapOf("titulos" to titulos))
                it.setVariables(mapOf("mail" to vendedor.getEmailAddress()))
            })
    }

    @GetMapping("/enviar")
    fun enviarInadimplencia() {
        salesService.getEnviaRelatorio().forEach{
            try{
                val titulos = service.getAllBySql(it.SalesEmployeeCode)
                val body = templateEngine.process("relatorio-inadiplencia",
                    Context().also { it.setVariables(mapOf("titulos" to titulos)) }
                )
                val data = SimpleDateFormat("dd/MM/yyy").format(Date())
                val titulo = "Relatório de Inadimplência do vendedor ${it.SalesEmployeeName} - ${data}"
                mailService.sendEmail(MyMailMessage(listOf(EmailAdrres(it.getEmailAddress())),titulo,body, From.COMERCIAL),true)
                telegramMsg.send("${titulo} enviado com sucesso",TipoMensagem.eventos)
            }catch (e : Exception){
                logger.error("Erro ao enviar relatório de inadimplência para o vendedor ${it.SalesEmployeeCode}",e)
            }
        }
    }

}