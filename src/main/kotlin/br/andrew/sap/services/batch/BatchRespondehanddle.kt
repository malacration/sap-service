package br.andrew.sap.services.batch

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class BatchRespondeHandler {

    private val jsonMapper = ObjectMapper()

    /**
     * Extrai (code, message) do corpo JSON de erro do SAP.
     *
     * Faz o parse real do JSON ao inves de regex porque a mensagem pode conter
     * aspas escapadas (ex.: This document row does not support the "Free of Charge ...")
     * e porque o no "details" contem um "message" vazio que vem antes da mensagem
     * real do erro - uma regex ingenua acabava casando com o vazio.
     */
    private fun extractError(changeset: String): Pair<String?, String?> {
        val jsonStart = changeset.indexOf('{')
        if (jsonStart < 0) return null to null

        val errorNode: JsonNode = try {
            jsonMapper.readTree(changeset.substring(jsonStart)).get("error")
        } catch (e: Exception) {
            null
        } ?: return null to null

        val code = errorNode.get("code")?.asText()?.takeIf { it.isNotBlank() }

        val messageNode = errorNode.get("message")
        val message = when {
            messageNode == null || messageNode.isNull -> null
            // SAP pode retornar message como objeto {"lang":..,"value":..} ou como string
            messageNode.isObject -> messageNode.get("value")?.asText()
            else -> messageNode.asText()
        }?.takeIf { it.isNotBlank() }

        return code to message
    }

    fun parseBatchResponse(batchResponse: String): List<BatchResponse> {
        // Delimitadores para separar os blocos de batch e changesets
        val batchDelimiter = "--batchresponse_[\\w-]+".toRegex()
        val changesetDelimiter = "--changesetresponse_[\\w-]+".toRegex()

        // Separamos primeiro pelos batch delimiters
        val batchParts = batchResponse.split(batchDelimiter)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val results = mutableListOf<BatchResponse>()

        for (batchPart in batchParts) {
            // Alguns batchParts podem conter vários changesets
            val changesets = batchPart.split(changesetDelimiter)
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            for (changeset in changesets) {
                // Regex pra achar status code
                val statusCodeRegex = """HTTP\/1\.1\s+(\d{3})""".toRegex()
                val contentIdRegex = """Content-ID:\s*(\d+)""".toRegex(RegexOption.IGNORE_CASE)

                val statusCode = statusCodeRegex.find(changeset)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull() ?: 0

                if (statusCode == 0) continue

                val contentId = contentIdRegex.find(changeset)?.groupValues?.get(1)?.toIntOrNull()

                val isSuccess = statusCode in 200..299

                if (isSuccess) {
                    results.add(
                        BatchResponse(
                            statusCode = statusCode,
                            body = changeset,
                            success = true,
                            contentId = contentId
                        )
                    )
                } else {
                    val (errCode, errMsg) = extractError(changeset)

                    results.add(
                        BatchResponse(
                            statusCode = statusCode,
                            body = changeset,
                            success = false,
                            errorCode = errCode,
                            errorMessage = errMsg,
                            contentId = contentId
                        )
                    )
                }
            }
        }
        return results
    }
}
