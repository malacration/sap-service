package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedReceivingOrderRequest
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SysfeedIntegratorClient(
    private val objectMapper: ObjectMapper,
    @Value("\${sysfeed.integrador.base-url:http://localhost:8080}") private val baseUrl: String
) {
    private val logger = LoggerFactory.getLogger(SysfeedIntegratorClient::class.java)
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun receivingOrderExists(nrProducao: String): Boolean {
        logJson(
            "sysfeed_integrator_receiving_order_lookup_started",
            mapOf("nrProducao" to nrProducao)
        )
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/sysfeed/ordens-recebimento/$nrProducao")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (response.code == 404) {
                logJson(
                    "sysfeed_integrator_receiving_order_lookup_finished",
                    mapOf(
                        "nrProducao" to nrProducao,
                        "statusCode" to response.code,
                        "exists" to false
                    )
                )
                return false
            }
            if (response.isSuccessful) {
                logJson(
                    "sysfeed_integrator_receiving_order_lookup_finished",
                    mapOf(
                        "nrProducao" to nrProducao,
                        "statusCode" to response.code,
                        "exists" to true
                    )
                )
                return true
            }
            val body = response.body?.string()
            logJson(
                "sysfeed_integrator_receiving_order_lookup_error",
                mapOf(
                    "nrProducao" to nrProducao,
                    "statusCode" to response.code,
                    "body" to body
                )
            )
            throw SysfeedIntegratorException("Erro ao consultar ordem de recebimento $nrProducao no integrador: HTTP ${response.code}")
        }
    }

    fun createReceivingOrder(payload: SysfeedReceivingOrderRequest) {
        logJson(
            "sysfeed_integrator_receiving_order_create_started",
            mapOf(
                "nrProducao" to payload.NrProducao,
                "payload" to payload
            )
        )
        val body = objectMapper.writeValueAsString(payload).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/sysfeed/ordens-recebimento")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                logJson(
                    "sysfeed_integrator_receiving_order_create_finished",
                    mapOf(
                        "nrProducao" to payload.NrProducao,
                        "statusCode" to response.code
                    )
                )
                return
            }
            val responseBody = response.body?.string().orEmpty()
            if (isDuplicate(responseBody)) {
                logJson(
                    "sysfeed_integrator_receiving_order_create_duplicated",
                    mapOf(
                        "nrProducao" to payload.NrProducao,
                        "statusCode" to response.code,
                        "body" to responseBody
                    )
                )
                return
            }
            logJson(
                "sysfeed_integrator_receiving_order_create_error",
                mapOf(
                    "nrProducao" to payload.NrProducao,
                    "statusCode" to response.code,
                    "body" to responseBody
                )
            )
            throw SysfeedIntegratorException("Erro ao enviar ordem de recebimento ${payload.NrProducao} ao integrador: HTTP ${response.code} - $responseBody")
        }
    }

    private fun logJson(event: String, fields: Map<String, Any?>) {
        logger.info(objectMapper.writeValueAsString(mapOf("event" to event) + fields))
    }

    private fun isDuplicate(body: String): Boolean {
        return body.uppercase().contains("ORDEM DE RECEBIMENTO JÁ EXISTE") ||
            body.uppercase().contains("ORDEM DE RECEBIMENTO JA EXISTE")
    }
}

class SysfeedIntegratorException(message: String) : RuntimeException(message)
