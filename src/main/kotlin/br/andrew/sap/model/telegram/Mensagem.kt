package br.andrew.sap.model.telegram

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Mensagem(val chat_id : String,
               val text : String,
               val message_thread_id : String?,
               val parse_mode : String = "html"){
    constructor(chat_id: String, text: String, tipo: TipoMensagem) : this(chat_id,text,tipo.topic().toString())

}
