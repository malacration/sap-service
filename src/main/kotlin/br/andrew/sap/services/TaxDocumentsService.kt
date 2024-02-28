package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.Session
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.envrioments.SapEnvrioment
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

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