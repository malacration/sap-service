package br.andrew.sap.services.structs

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.Query
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate


@Service
class QuerysServices(env: SapEnvrioment, restTemplate: RestTemplate,
                     authService: AuthService) : EntitiesService<Query>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/SQLQueries"
    }

    fun replace(query: Query) {
        try {
            val code = "'${query.sqlCode}'"
            this.getById(code)
            this.update(query,"$code")
        }catch (t : HttpClientErrorException){
            if(t.statusCode == org.springframework.http.HttpStatus.NOT_FOUND){
                this.save(query)
            }
            t.printStackTrace()
        }
    }

//    fun expose(viewName: String): OData? {
//        val request = RequestEntity
//            .post(env.host+"/b1s/v1/SQLViews('$viewName')/Expose")
//            .header("cookie","B1SESSION=${session().sessionId}")
//            .build()
//        return restTemplate.exchange(request, OData::class.java).body
//
//    }
}
