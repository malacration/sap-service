package br.andrew.sap.controllers


import br.andrew.sap.model.Version
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.ContratoVendaFuturaService
import br.andrew.sap.services.MailService
import br.andrew.sap.services.MyMailMessage
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.security.UserPasswordService
import br.andrew.sap.services.structs.QuerysServices
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class IndexController(
    val version : Version,
    val teste : QuerysServices,
    val passowrdService: UserPasswordService,
    val service : BatchService,
){

    val logger = LoggerFactory.getLogger(IndexController::class.java)

    @GetMapping("/")
    fun index() : Version{
        teste.teste()
        return version
    }

    @PostMapping("/logar")
    fun forCorsOrigin() {
        println("EndPoint for optionals login")
    }

    @PostMapping("/change-password")
    fun changePassword(@RequestBody password : String, auth : Authentication) {
        if(auth is User)
            passowrdService.changePassword(auth,password)
    }
}
