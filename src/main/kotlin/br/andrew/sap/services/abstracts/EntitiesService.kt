package br.andrew.sap.services.abstracts

import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.SapError
import br.andrew.sap.model.sap.Session
import br.andrew.sap.services.AuthService
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URLDecoder

open abstract class EntitiesService<T>(protected val env: SapEnvrioment,
                                  protected val restT: RestTemplate,
                                  protected val authService: AuthService) : EntitiesBase{
    override fun session() : Session {
        return authService.getToken(env.getLogin())
    }

    override fun getHost(): String {
        return env.host
    }

    override fun getRestTemplate(): RestTemplate {
        return restT
    }

    open fun save(entry: T & Any) : OData {
        try{
            val request = RequestEntity
                    .post(env.host+this.path())
                    .header("cookie","B1SESSION=${session().sessionId}")
                    .body(entry)
            return restT.exchange(request, OData::class.java).body!!
        }catch (t : HttpClientErrorException){
            throw t.getResponseBodyAs(SapError::class.java)?.getError(t,entry) ?: t
        }
    }

    fun update(entry : Any, id : String): OData?{
        val idNew = if(id.toIntOrNull() == null && id[0] != "'"[0])
            "'$id'"
        else
            id
        val request = RequestEntity
                .patch(env.host+this.path()+"($idNew)")
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(entry)
        return restT.exchange(request, OData::class.java).body
    }

    fun put(entry : Any, id : String): OData?{
        val request = RequestEntity
            .put(env.host+this.path()+"($id)")
            .header("cookie","B1SESSION=${session().sessionId}")
            .body(entry)
        return restT.exchange(request, OData::class.java).body
    }

    fun cancel(id : String): OData? {
        val request = RequestEntity
                .post(env.host+this.path()+"($id)/Cancel")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restT.exchange(request, OData::class.java).body
    }

    open fun get(filter : Filter = Filter(), order : OrderBy = OrderBy(), page : Pageable = Pageable.ofSize(20)) : OData {
        val aditional = listOf(filter,order).filter { it.toString().isNotEmpty() }.joinToString("&","&")
        val skip = (page.pageNumber*page.pageSize).toString()+"&\$inlinecount=allpages"
        val request = RequestEntity
                .get(env.host+this.path()+"?\$skip=${skip}"+aditional)
                .header("cookie","B1SESSION=${session().sessionId}")
                .header("Prefer",  "odata.maxpagesize=${page.pageSize}")
                .build()
        return restT.exchange(request, OData::class.java).body ?: OData()
    }

    //Se nao for int adicionar aspas! Se ja existir aspas nao fazer nada.
    open fun getById(id : String) : OData {
        try{
            val idNew = if(id.toIntOrNull() == null && id[0] != "'"[0])
                "'$id'"
            else
                id
            val request = RequestEntity
                .get(env.host+this.path()+"(${idNew})")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
            return restT.exchange(request, OData::class.java).body!!
        }catch (t : HttpClientErrorException){
            throw t.getResponseBodyAs(SapError::class.java)?.getError(t,id) ?: t
        }
    }

    open fun getById(id : Int) : OData {
        return getById(id.toString())
    }

    fun delete(id : String) : OData? {
        val request = RequestEntity
            .delete(env.host+this.path()+"(${id})")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restT.exchange(request, OData::class.java).body
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

    fun <T> getAllNextBy(inicialOdata : OData, clazz: Class<T>) : List<T> {
        var odata = inicialOdata
        var content : MutableList<T> = mutableListOf()
        content.addAll(odata.tryGetValues(clazz))
        while (odata.hasNext()){
            odata = next(odata)
            content.addAll(odata.tryGetValues(clazz))
        }
        return content;
    }

    open fun next(odata : OData) : OData {
        return next(odata.nextLink())
    }

    open fun next(next : String) : OData {
        val regex = "/([^/]+)$".toRegex()
        val url = (env.host+this.path()).replace(regex,"/")+next
        val request = RequestEntity
            .get(URLDecoder.decode(url,"UTF-8"))
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restT.exchange(request, OData::class.java).body!!
    }

    fun <T> getAll(clazz: Class<T>, filter: Filter = Filter()): List<T> {
        var content : MutableList<T> = mutableListOf()
        var odata : OData = get(filter)
        content.addAll(odata.tryGetValues(clazz))
        while (odata.hasNext()){
            odata = next(odata)
            content.addAll(odata.tryGetValues(clazz))
        }
        return content;
    }

    fun cru(body: String): OData {
        val request = RequestEntity
                .post(env.host+this.path())
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(body)
        return restT.exchange(request, OData::class.java).body!!
    }

    fun cruUp(body: String, id : String): OData{
        val request = RequestEntity
                .patch(env.host+this.path()+"($id)")
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(body)
        return restT.exchange(request, OData::class.java).body!!
    }

    fun serviceCancel(payload : String) {
        val request = RequestEntity
            .post(env.host+this.path()+"Service_Cancel")
            .header("cookie","B1SESSION=${session().sessionId}")
            .body(payload)
        restT.exchange(request, Void::class.java)
    }

    fun crossJoin(entidades : List<Entidade>, filter : Filter = Filter(),
                  order : OrderBy = OrderBy(), page : Pageable = Pageable.ofSize(20)): OData {
        var crossjoin = "/b1s/v1/\$crossjoin(${entidades.joinToString(",") { it.entidadeNome }})"
        var expand = "\$expand=${entidades.joinToString(",") { it.getExpand() }}";

        //TODO adicionar order by
        val filter = listOf(filter).filter { it.toString().isNotEmpty() }.joinToString("&").replace("\$filter=","\$filter=(")+")"
        val skip = (page.pageNumber*page.pageSize).toString()+"&\$inlinecount=allpages"
        var path = "${crossjoin}?${expand}&$filter"

        var groupBy = ""
        var aggregation = ""

        //TODO precisa terminar esse agreggation
        if(entidades.any{ it.aggregation }){
            aggregation = "aggregate(AR_CONTRATO_FUTURO/AR_CF_LINHACollection((U_quantity add 10) with sum as qtdSoma))"
            groupBy = "groupby((${entidades.filter { !it.aggregation }.joinToString(",") { it.getExpandGroupBy() }}),$aggregation)"
            val newFilter = filter.replace("\$filter=","filter(")+")"
            path = "${crossjoin}?\$apply=$newFilter/$groupBy&"+order.toString()
            throw Exception("agregação ainda nao é suportada")
        }
        val request = RequestEntity
            .get(env.host+path+"&\$skip=${skip}")
            .header("cookie","B1SESSION=${session().sessionId}")
            .header("Prefer",  "odata.maxpagesize=${page.pageSize}")
            .build()
        return restT.exchange(request, OData::class.java).body ?: OData()
    }


}






