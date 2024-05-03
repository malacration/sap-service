package br.andrew.sap.services.abstracts

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.Session
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SqlQueriesService(
    private val env : SapEnvrioment,
    private val restTemplate: RestTemplate,
    private val authService: AuthService
) {

    private fun session() : Session {
        return authService.getToken(env.getLogin())
    }

    private val url = env.host+"/b1s/v1"

    fun execute(viewName: String, p : Parameter): OData? {
        return execute(viewName, listOf(p))
    }

    fun execute(viewName : String, parameters : List<Parameter> = listOf()): OData? {
        val parametros = if(parameters.isNotEmpty()) "?"+getParameter(parameters) else ""
        val request = RequestEntity
            .get("$url/SQLQueries('$viewName')/List$parametros")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restTemplate.exchange(request, OData::class.java).body
    }

    fun nextLink(nextLink : String): OData? {
        val request = RequestEntity
            .get("$url/$nextLink")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restTemplate.exchange(request, OData::class.java).body
    }

    fun getParameter(p : List<Parameter>) : String{
        return p.joinToString("&")
    }
}