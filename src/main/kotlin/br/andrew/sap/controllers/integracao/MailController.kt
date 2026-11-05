package br.andrew.sap.controllers.integracao
import br.andrew.sap.model.sap.cadastro.SalePerson
import br.andrew.sap.services.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import br.andrew.sap.services.integracao.MailService
import br.andrew.sap.services.cadastro.SalesPersonsService
import br.andrew.sap.services.comercial.ParcelasAbertoService
import br.andrew.sap.services.integracao.MyMailMessage
import br.andrew.sap.services.integracao.AgendaRelatorioService

@RestController
@RequestMapping("mail")
class MailController(val mailService: MailService,
                     val templateEngine : TemplateEngine,
                     val salesService : SalesPersonsService,
                     val service: ParcelasAbertoService,
                     val agenda: AgendaRelatorioService) {


    val logger: Logger = LoggerFactory.getLogger(MailController::class.java)

    @GetMapping("{mail}")
    fun teste(@PathVariable mail : String){
        mailService.sendEmail(MyMailMessage(mail,"Email teste - SAP Service","Ola essa é uma mensagem de teste"))
    }

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
        if (!agenda.enviarAgora(null)) throw ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um envio em andamento.")
    }

}
