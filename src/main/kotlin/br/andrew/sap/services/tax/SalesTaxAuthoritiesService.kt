package br.andrew.sap.services.tax

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.SalesTaxCodeLine
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SalesTaxAuthoritiesService(val env: SapEnvrioment,
                                 val restTemplate: RestTemplate,
                                 val authService: AuthService) {


    fun path(): String {
        return "/b1s/v1/SalesTaxAuthorities"
    }

    fun get(id : String, type : Int) : OData {
        val session = authService.getToken(env.getLogin())
        val request = RequestEntity
                .get(env.host+this.path()+"(Code='$id',Type=$type)")
                .header("cookie","B1SESSION=${session.sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    fun get(sales : SalesTaxCodeLine) : OData {
        return get(sales.STACode,sales.STAType)
    }


}