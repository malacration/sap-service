package br.andrew.sap.services.batch

import br.andrew.sap.model.envrioments.SapEnvrioment
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
    private val lineBreak = "\r\n"

    fun path(): String {
        return "/b1s/v1/\$batch"
    }

    fun body(batchUUID : String, bathList: BatchList): ByteArray {
        return body(batchUUID, listOf(bathList))
    }

    fun body(batchUUID: String, batchGroups: List<BatchList>): ByteArray {
        val body = batchGroups
            .filter { it.isNotEmpty() }
            .map { getBatchGroup(batchUUID, it) }
            .fold(ByteArray(0)) { acc, byteArray ->
                acc + byteArray
            }
        val footer = "--batch_$batchUUID--"
        return body.plus(footer.toByteArray())
    }

    private fun getBatchGroup(batchUUID: String, batchList: BatchList): ByteArray {
        val changesetUUID = "changeset_" + UUID.randomUUID().toString()
        val header = (
            "--batch_$batchUUID$lineBreak" +
                "Content-Type: multipart/mixed;boundary=$changesetUUID$lineBreak$lineBreak"
            ).toByteArray()
        val content = batchList.mapIndexed { index, item ->
            getChangeSet(changesetUUID, item, index + 1).toByteArray()
        }.fold(ByteArray(0)) { acc, byteArray -> acc + byteArray }
        val footer = "--$changesetUUID--$lineBreak"
        return header.plus(content).plus(footer.toByteArray())
    }

    private fun getChangeSet(changeSetUUID : String, item : Triple<BatchMethod,Any, EntitiesService<*>>, contentId : Int) : String{
        val json = ObjectMapper().registerModule(KotlinModule()).writeValueAsString(item.second)
        val batchId : BatchId? = if(item.second is BatchId) item.second as BatchId else null
        return getChangeSet(changeSetUUID,item.first.getHttp(item.third,batchId),json,contentId)
    }

    private fun getChangeSet(changeSetUUID : String, http : String, json : String, contentId : Int): String {
        val hasRequestBody = !http.endsWith("Cancel") && !http.endsWith("Close")
        val requestHeaders = if (hasRequestBody)
            "Content-Type: application/json$lineBreak"
        else
            "Content-Length: 0$lineBreak"
        val requestBody = if (hasRequestBody) "$lineBreak$json$lineBreak" else lineBreak

        return "--$changeSetUUID$lineBreak" +
                "Content-Type: application/http$lineBreak"+
                "Content-Transfer-Encoding: binary$lineBreak" +
                "Content-ID: $contentId$lineBreak$lineBreak" +
                "$http HTTP/1.1$lineBreak" +
                requestHeaders +
                requestBody
    }

    fun run(bathList: BatchList): List<BatchResponse> {
        return run(listOf(bathList))
    }

    fun run(batchGroups: List<BatchList>): List<BatchResponse> {
        val batchUUID = UUID.randomUUID().toString()
        val body = body(batchUUID, batchGroups)
        val retorno = authService.executeWithValidSession(env.getLogin()) { session ->
            val request = RequestEntity
                .post(env.host+this.path())
                .header("Content-Type","multipart/mixed;boundary=batch_$batchUUID")
                .header("OData-Version","4.0")
                .header("cookie","B1SESSION=${session.sessionId}")
                .header("Content-Length",body.size.toString())
            rest.exchange(request.body(body), String::class.java).body
        }
        val resposta = BatchRespondeHandler().parseBatchResponse(retorno!!)
        val erros = resposta.withIndex().filter { !it.value.success }
        if (erros.isNotEmpty()) {
            val mensagem = erros.joinToString("\n") { indexedResponse ->
                val response = indexedResponse.value
                val erro = if ((response.errorMessage ?: "").contains("awaiting approval"))
                    "documento está aguardando aprovação e não pode ser alterado"
                else
                    response.errorMessage ?: response.body ?: "Erro ${response.statusCode}"
                erro
            }
            throw Exception(mensagem)
        }
        return resposta
    }


}
