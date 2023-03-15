package br.andrew.sap.rovema.services.abstracts

import br.andrew.sap.rovema.infrastructure.odata.Filter
import br.andrew.sap.rovema.infrastructure.odata.OData
import br.andrew.sap.rovema.infrastructure.odata.OrderBy
import br.andrew.sap.rovema.model.SapEnvrioment
import br.andrew.sap.rovema.model.Session
import br.andrew.sap.rovema.services.AuthService
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate

abstract class EntitiesService<T>(protected val env: SapEnvrioment,
                                  protected val restTemplate: RestTemplate,
                                  protected val authService: AuthService) {
    fun session() : Session{
        return authService.getToken(env.getLogin())
    }
    abstract fun path() : String

    fun save(entry: T & Any) : OData {
        val request = RequestEntity
                .post(env.host+this.path())
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(entry)
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    fun update(entry : T & Any, id : String): OData?{
        val request = RequestEntity
                .patch(env.host+this.path()+"($id)")
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(entry)
        return restTemplate.exchange(request, OData::class.java).body
    }

    fun get(filter : Filter = Filter(), order : OrderBy = OrderBy()) : OData {
        val aditional = listOf(filter,order).filter { it.toString().isNotEmpty() }.joinToString(" ")
        val request = RequestEntity
                .get(env.host+this.path()+"?"+aditional)
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    fun getById(id : String) : OData {
        val request = RequestEntity
                .get(env.host+this.path()+"('${id}')")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }


    fun get() : OData {
        return get(OrderBy())
    }

    fun get(order : OrderBy) : OData {
        return get(Filter(),order)
    }

    fun get(filter : Filter) : OData {
        return get(filter, OrderBy())
    }

    fun cru(body: String): OData {
        val request = RequestEntity
                .post(env.host+this.path())
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(body)
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    fun cruUp(body: String, id : String): OData{
        val request = RequestEntity
                .patch(env.host+this.path()+"($id)")
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(body)
        return restTemplate.exchange(request, OData::class.java).body!!
    }
}






