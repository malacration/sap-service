package br.andrew.sap.services.abstracts

import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.DocEntry
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.SapError
import br.andrew.sap.model.Session
import br.andrew.sap.services.AuthService
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URLDecoder

abstract class EntitiesService<T>(protected val env: SapEnvrioment,
                                  protected val restTemplate: RestTemplate,
                                  protected val authService: AuthService) {
    fun session() : Session{
        return authService.getToken(env.getLogin())
    }
    abstract fun path() : String

    open fun save(entry: T & Any) : OData {
        try{
            val request = RequestEntity
                    .post(env.host+this.path())
                    .header("cookie","B1SESSION=${session().sessionId}")
                    .body(entry)
            return restTemplate.exchange(request, OData::class.java).body!!
        }catch (t : HttpClientErrorException){
            throw t.getResponseBodyAs(SapError::class.java)?.getError(t,entry) ?: t
        }
    }

    fun update(entry : Any, id : String): OData?{
        val request = RequestEntity
                .patch(env.host+this.path()+"($id)")
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(entry)
        return restTemplate.exchange(request, OData::class.java).body
    }

    fun put(entry : Any, id : String): OData?{
        val request = RequestEntity
            .put(env.host+this.path()+"($id)")
            .header("cookie","B1SESSION=${session().sessionId}")
            .body(entry)
        return restTemplate.exchange(request, OData::class.java).body
    }

    fun cancel(id : String): OData? {
        val request = RequestEntity
                .post(env.host+this.path()+"($id)/Cancel")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body
    }

    open fun get(filter : Filter = Filter(), order : OrderBy = OrderBy(), page : Pageable = Pageable.ofSize(20)) : OData {
        val aditional = listOf(filter,order).filter { it.toString().isNotEmpty() }.joinToString("&","&")
        val skip = (page.pageNumber*page.pageSize).toString()+"&\$inlinecount=allpages"
        val request = RequestEntity
                .get(env.host+this.path()+"?\$skip=${skip}"+aditional)
                .header("cookie","B1SESSION=${session().sessionId}")
                .header("Prefer",  "odata.maxpagesize=${page.pageSize}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body ?: OData()
    }

    fun getById(id : String) : OData {
        val request = RequestEntity
                .get(env.host+this.path()+"(${id})")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    open fun getById(id : Int) : OData {
        val request = RequestEntity
                .get(env.host+this.path()+"(${id})")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    fun delete(id : String) : OData {
        val request = RequestEntity
            .delete(env.host+this.path()+"(${id})")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    fun getBy(doc : DocEntry) : OData {
        return getById(doc.DocEntry ?: throw Exception("DocEntry não pode ser nulo"))
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

    fun get(filter: Filter, page: Pageable): OData {
        return get(filter, OrderBy(), page)
    }

    fun next(odata : OData) : OData {
        return next(odata.nextLink())
    }

    fun next(next : String) : OData {
        val regex = "/([^/]+)$".toRegex()
        val url = (env.host+this.path()).replace(regex,"/")+next
        val request = RequestEntity
            .get(URLDecoder.decode(url,"UTF-8"))
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    inline fun <reified T> getAll(filter: Filter = Filter()): List<T> {
        var content : MutableList<T> = mutableListOf()
        var odata : OData = get(filter)
        content.addAll(odata.tryGetValues())
        while (odata.hasNext()){
            odata = next(odata)
            content.addAll(odata.tryGetValues())
        }
        return content;
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






