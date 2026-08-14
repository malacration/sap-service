package br.andrew.sap.controllers.sistema
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.dto.MeuUsuarioDto
import br.andrew.sap.model.dto.VendedorVinculadoDto
import br.andrew.sap.model.sap.cadastro.SalePerson
import br.andrew.sap.model.sistema.Version
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.cadastro.SalesPersonsService
import br.andrew.sap.services.comercial.ContratoVendaFuturaService
import br.andrew.sap.services.integracao.MailService
import br.andrew.sap.services.integracao.MyMailMessage
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.documents.CreditNotesService
import br.andrew.sap.services.documents.DownPaymentService
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
    val sqlQueriesService: SqlQueriesService,
    val salesPersonsService: SalesPersonsService
){

    val logger = LoggerFactory.getLogger(IndexController::class.java)

    @GetMapping("/")
    fun index() : Version{
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

    //dados do usuario logado - inclui o vendedor vinculado (se houver), pra
    //conferir a alcada de credito/comissao/liberacao sem precisar entrar no SAP
    @GetMapping("/me")
    fun me(auth : Authentication) : MeuUsuarioDto {
        val user = auth as User
        val vendedor = if(user.origin == UserOriginEnum.SalePerson)
            runCatching { salesPersonsService.getById(user.id).tryGetValue<SalePerson>() }
                .getOrNull()
                ?.let { VendedorVinculadoDto(it.SalesEmployeeCode, it.SalesEmployeeName) }
        else null
        return MeuUsuarioDto(
            id = user.id,
            userName = user.userName,
            emailAddress = user.emailAddress,
            origin = user.origin.name,
            bussinesPlace = user.bussinesPlace,
            roles = user.roles,
            vendedor = vendedor
        )
    }

    @PostMapping("/nextlink")
    fun nextLink(@RequestBody query: String) : NextLink<Any> {
        return sqlQueriesService.nextLink(query)?.tryGetNextValues<Any>() ?: throw Exception("Conteudo nao encontrado")
    }
}
