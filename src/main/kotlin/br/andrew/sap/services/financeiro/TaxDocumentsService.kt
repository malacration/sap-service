package br.andrew.sap.services.financeiro
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sistema.SapEnvrioment
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService

@Service
class TaxDocumentsService(val env : SapEnvrioment,
                          val restTemplate: RestTemplate,
                          val authService: AuthService){

    fun getKeyNfe(docEntry : String) : OData {
        val url = env.host+"/b1s/v1/SQLQueries"
        val pagamentos = authService.executeWithValidSession(env.getLogin()) { session ->
            restTemplate.exchange(
                RequestEntity
                    .get("$url('invoice-fiscal-info.sql')/List?DocEntry=$docEntry")
                    .header("cookie", session.cookieHeader())
                    .build(), OData::class.java
            ).body!!
        }
        return pagamentos
    }

    fun getNfeFile(document: Document, s: String): ByteArray {
        return "windson".toByteArray()
    }
}
