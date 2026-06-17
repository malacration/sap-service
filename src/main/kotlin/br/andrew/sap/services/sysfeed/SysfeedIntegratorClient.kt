package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedReceivingOrderRequest
import br.andrew.sap.model.sysfeed.SysfeedSupplierRequest
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
    @Value("\${sysfeed.integrador.base-url:http://localhost:8181}") private val baseUrl: String
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
                val body = response.body?.string().orEmpty()
                val exists = receivingOrderLookupFound(nrProducao, body)
                logJson(
                    "sysfeed_integrator_receiving_order_lookup_finished",
                    mapOf(
                        "nrProducao" to nrProducao,
                        "statusCode" to response.code,
                        "exists" to exists,
                        "body" to body
                    )
                )
                return exists
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

    fun createSupplier(payload: SysfeedSupplierRequest) {
        logJson(
            "sysfeed_supplier_create_started",
            mapOf(
                "cardCode" to payload.cardCode,
                "payload" to payload
            )
        )
        val body = objectMapper.writeValueAsString(payload).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/sysfeed/fornecedores")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                logJson(
                    "sysfeed_supplier_create_finished",
                    mapOf(
                        "cardCode" to payload.cardCode,
                        "statusCode" to response.code
                    )
                )
                return
            }
            val responseBody = response.body?.string().orEmpty()
            logJson(
                "sysfeed_supplier_create_error",
                mapOf(
                    "cardCode" to payload.cardCode,
                    "statusCode" to response.code,
                    "body" to responseBody
                )
            )
            throw SysfeedIntegratorException("Erro ao enviar fornecedor ${payload.cardCode} ao Sigafran: HTTP ${response.code} - $responseBody")
        }
    }

    fun getSupplier(identifier: String): String {
        logJson("sysfeed_supplier_lookup_started", mapOf("identifier" to identifier))
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/sysfeed/fornecedores/$identifier")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                logJson(
                    "sysfeed_supplier_lookup_finished",
                    mapOf(
                        "identifier" to identifier,
                        "statusCode" to response.code
                    )
                )
                return responseBody
            }
            logJson(
                "sysfeed_supplier_lookup_error",
                mapOf(
                    "identifier" to identifier,
                    "statusCode" to response.code,
                    "body" to responseBody
                )
            )
            throw SysfeedIntegratorException("Erro ao consultar fornecedor $identifier no Sigafran: HTTP ${response.code} - $responseBody")
        }
    }

    fun getSuppliers(): String {
        logJson("sysfeed_supplier_list_started", emptyMap())
        val request = Request.Builder()
            .url("${baseUrl.trimEnd('/')}/api/sysfeed/fornecedores")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                logJson("sysfeed_supplier_list_finished", mapOf("statusCode" to response.code))
                return responseBody
            }
            logJson(
                "sysfeed_supplier_list_error",
                mapOf(
                    "statusCode" to response.code,
                    "body" to responseBody
                )
            )
            throw SysfeedIntegratorException("Erro ao listar fornecedores no Sigafran: HTTP ${response.code} - $responseBody")
        }
    }

    private fun logJson(event: String, fields: Map<String, Any?>) {
        logger.info(objectMapper.writeValueAsString(mapOf("event" to event) + fields))
    }

    private fun isDuplicate(body: String): Boolean {
        return body.uppercase().contains("ORDEM DE RECEBIMENTO JÁ EXISTE") ||
            body.uppercase().contains("ORDEM DE RECEBIMENTO JA EXISTE")
    }

    internal fun receivingOrderLookupFound(nrProducao: String, body: String): Boolean {
        val trimmed = body.trim()
        if (trimmed.isBlank() || trimmed == "{}" || trimmed == "[]" || trimmed.equals("null", ignoreCase = true)) {
            return false
        }
        return trimmed.contains(nrProducao)
    }
}

class SysfeedIntegratorException(message: String) : RuntimeException(message)
