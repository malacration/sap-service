package br.andrew.sap.services.abstracts

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.Session
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class TesteService(
    private val env : SapEnvrioment,
    private val restTemplate: RestTemplate,
    private val authService: AuthService
) {

    private fun session() : Session {
        return authService.getToken(env.getLogin())
    }

    private val url = env.host+"/b1s/v1"

    open fun get() : OData {
        val request = RequestEntity
            .get(env.host+"/b1s/v1/sml.svc")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restTemplate.exchange(request, OData::class.java).body ?: OData()
    }
}