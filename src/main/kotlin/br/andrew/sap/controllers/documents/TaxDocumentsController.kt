package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.configurations.security.otp.User
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.DocEntry
import br.andrew.sap.model.Session
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.documents.DocumentStatus
import br.andrew.sap.model.documents.Fatura
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.*
import br.andrew.sap.services.document.InvoiceService
import brave.Tracer
import jdk.jfr.StackTrace
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import java.util.*

@RestController
@RequestMapping("tax-documents")
class TaxDocumentsController(
    val tracer: Tracer,
    val taxService : TaxDocumentsService,
    val test : BankPlusService,
    val invoice: InvoiceService) {

    val logger = LoggerFactory.getLogger(TaxDocumentsController::class.java)

    @GetMapping()
    fun teste(user : Authentication) : Any{
        if(user !is User)
            throw Exception("")
        val docEntry = 25991
        val document = invoice.getById(docEntry).tryGetValue<Document>()
        if(user.id != "windsonCPF")
            throw AccessDeniedException("Usuario nao tem permissao para acessar NFe")

        val retorno = taxService.getKeyNfe(docEntry.toString());

        return taxService.getNfeFile(document,"chaveAcesso")
    }
}

@Service
class TaxDocumentsService(val env : SapEnvrioment,
                          val restTemplate: RestTemplate,
                          val authService: AuthService){

    fun session(): Session {
        return authService.getToken(env.getLogin())
    }

    fun getKeyNfe(docEntry : String) : OData {
        val url = env.host+"/b1s/v1/SQLQueries"
        val pagamentos = restTemplate.exchange(
            RequestEntity
                .get("$url('invoice-fiscal-info.sql')/List?DocEntry=$docEntry")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build(), OData::class.java).body!!
        return pagamentos
    }

    fun getNfeFile(document: Document, s: String): ByteArray {
        return "windson".toByteArray()
    }
}
