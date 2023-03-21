package br.andrew.sap.services.abstracts

import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.Session
import br.andrew.sap.services.AuthService
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Pageable
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

    fun get(filter : Filter = Filter(), order : OrderBy = OrderBy(), page : Pageable = Pageable.ofSize(20)) : OData {
        val aditional = listOf(filter,order).filter { it.toString().isNotEmpty() }.joinToString(" ","&")
        val skip = page.pageNumber*page.pageSize;
        val request = RequestEntity
                .get(env.host+this.path()+"?skip=${skip}"+aditional)
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

    fun getById(id : Int) : OData {
        val request = RequestEntity
                .get(env.host+this.path()+"(${id})")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    fun get(page : Pageable) : OData {
        return get(Filter(),OrderBy(),page)
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






