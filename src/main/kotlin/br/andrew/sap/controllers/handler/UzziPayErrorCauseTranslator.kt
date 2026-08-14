package br.andrew.sap.controllers.handler

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.client.HttpClientErrorException

object UzziPayErrorCauseTranslator {

    private val mapper = ObjectMapper()

    fun causeBy(t: Throwable): String? {
        return causes(t).firstNotNullOfOrNull { translate(it) } ?: t.cause?.message
    }

    private fun causes(t: Throwable): Sequence<Throwable> {
        return generateSequence(t) { it.cause }.drop(1)
    }

    private fun translate(t: Throwable): String? {
        if (t !is HttpClientErrorException) {
            return null
        }

        val body = t.responseBodyAsString.takeIf { it.isNotBlank() } ?: return t.message
        val node = try {
            mapper.readTree(body)
        } catch (ex: Throwable) {
            return t.message
        }

        val errorsNode = node.path("errors")
        if (!errorsNode.isArray) {
            return null
        }

        val errors = errorNodes(errorsNode)
        val emailError = errors.firstOrNull { error ->
            error.path("field").asText().equals("email", ignoreCase = true) ||
                error.path("code").asText().equals("Email", ignoreCase = true) ||
                error.path("description").asText().contains("'Email'", ignoreCase = true)
        }

        if (emailError != null) {
            return "E-mail do cliente invalido para gerar Pix na Uzzipay."
        }

        val message = node.path("message").asText("")
        val descriptions = errors
            .map { it.path("description").asText("") }
            .filter { it.isNotBlank() }

        return when {
            message.isNotBlank() && descriptions.isNotEmpty() -> "$message: ${descriptions.joinToString("; ")}"
            message.isNotBlank() -> message
            descriptions.isNotEmpty() -> descriptions.joinToString("; ")
            else -> body
        }
    }

    private fun errorNodes(errors: JsonNode): List<JsonNode> {
        if (!errors.isArray) {
            return emptyList()
        }
        return errors.toList()
    }
}
