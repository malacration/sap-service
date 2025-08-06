package br.andrew.sap.services.batch

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Session
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.abstracts.EntitiesService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class BatchService(val rest : RestTemplate,
                   val env: SapEnvrioment,
                   val bpService: BusinessPartnersService,
                   val authService : AuthService
) {

    fun path(): String {
        return "/b1s/v1/\$batch"
    }

    private fun session() : Session {
        return authService.getToken(env.getLogin())
    }



    fun body(batchUUID : String, bathList: BatchList): ByteArray {
        val changesetUUID = "changeset_"+ UUID.randomUUID().toString();
        val headeyBoundary = (
                "--batch_$batchUUID\n" +
                        "Content-Type: multipart/mixed;boundary=$changesetUUID\n\n"
                )

        val changesets = bathList.mapIndexed { index: Int, it: Triple<BatchMethod,Any, EntitiesService<*>> ->
            getChangeSet(changesetUUID, it,index+1)
        }
        val footer =
            "--changeset_$changesetUUID--\n"+
                    "--batch_$batchUUID--"
        return headeyBoundary.toByteArray().plus(
            changesets.map { it.toByteArray() }.fold(ByteArray(0)) { acc, byteArray ->
                acc + byteArray
            }
        ).plus(footer.toByteArray())
    }

    private fun getChangeSet(changeSetUUID : String, item : Triple<BatchMethod,Any, EntitiesService<*>>, contentId : Int) : String{
        val json = ObjectMapper().registerModule(KotlinModule()).writeValueAsString(item.second)
        val batchId : BatchId? = if(item.second is BatchId) item.second as BatchId else null
        return getChangeSet(changeSetUUID,item.first.getHttp(item.third,batchId),json,contentId)
    }

    private fun getChangeSet(changeSetUUID : String, http : String, json : String, contentId : Int): String {

        return "--$changeSetUUID\n"+
                "Content-Type: application/http\n"+
                "Content-Transfer-Encoding: binary\n" +
                "Content-ID: $contentId\n\n" +
                "$http\n\n" +
                if(http.endsWith("Cancel")) "" else "$json\n"
    }

    fun run(bathList: BatchList): List<BatchResponse> {
         val batchUUID = UUID.randomUUID().toString()
        var body = body(batchUUID,bathList)
        val request = RequestEntity
            .post(env.host+this.path())
            .header("Content-Type","multipart/mixed;boundary=batch_$batchUUID")
            .header("OData-Version","4.0")
            .header("cookie","B1SESSION=${session().sessionId}")
            .header("Content-Length",body.size.toString())
        val retorno = rest.exchange(request.body(body), String::class.java).body
        val resposta = BatchRespondeHandler().parseBatchResponse(retorno!!)
        resposta.filter { !it.success}
            .map { it.errorMessage }
            .reduce { str, batchResponse -> "$batchResponse. \n$str" }
            .also {
                if(it?.isNotEmpty() == true)
                    throw Exception(it)
            }
        return resposta
    }
}

