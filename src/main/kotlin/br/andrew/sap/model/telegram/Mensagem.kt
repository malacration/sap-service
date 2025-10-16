package br.andrew.sap.model.telegram

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.apache.commons.text.StringEscapeUtils

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Mensagem(
    val chat_id: String,
    text: String,
    val message_thread_id: String?,
    val parse_mode: String = "html"
) {
    val text: String = sanitize(text)

    constructor(chat_id: String, text: String, tipo: TipoMensagem) :
            this(chat_id, text, tipo.topic().toString())

    companion object {
        private fun sanitize(msg: String): String {
            return StringEscapeUtils.escapeHtml4(msg)
        }
    }
}
