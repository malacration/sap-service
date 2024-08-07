package br.andrew.sap.controllers.authentication

import br.andrew.sap.model.sap.partner.BusinessPartnerType
import br.andrew.sap.services.*
import br.andrew.sap.services.security.OneTimePasswordService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("otp")
class OneTimePasswordController(val bpService : BusinessPartnersService,
                                @Value("\${spring.security.disable:false}") val disable: Boolean,
                                val mailService: MailService,
                                val telegramService : TelegramRequestService,
                                val otpService : OneTimePasswordService
) {

    @GetMapping("/cpf-cnpj/{cpfCnpj}")
    fun getBy(@PathVariable cpfCnpj : String,
              @RequestParam(name = "otp", defaultValue = "cCustomer") otp : String,
              @RequestParam(name = "type", defaultValue = "cCustomer") tipo : BusinessPartnerType
    ) {
        val cliente = bpService.getByCpfCnpj(cpfCnpj,tipo)
        val contato = cliente
            .getContactOpaque()
            .filter { otp == it.contato }
            .firstOrNull() ?: throw Exception("Erro ao localizar o contato")
        val password = otpService.getOneTimePassword(cpfCnpj).passwrod
        if(!disable){
            mailService
                .sendEmail(MyMailMessage(contato.getTrueValue(),"Codigo de acesso","seu codigo é: $password"))
        }
        telegramService.send("Gerando OTP para: $cpfCnpj com codigo de ${password}")
    }

    @PostMapping("/login")
    fun forCorsOrigin() {
        println("EndPoint for optionals login")
    }


}