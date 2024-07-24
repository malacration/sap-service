package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.SapUser
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UserService(env: SapEnvrioment,
                  restTemplate: RestTemplate,
                  authService: AuthService) :
        EntitiesService<PurchaseInvoice>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/UsersService"
    }


    fun getCurretuser() : SapUser {
        val request = RequestEntity
                .post(env.host+this.path()+"_GetCurrentUser")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body?.tryGetValue() ?: SapUser(-1)
    }
}