package br.andrew.sap.controllers.handler

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ErroDto(val mensagem : String, val traceId : String) {

    var stackTrace : String? = null
    var causeBy : String? = null
    constructor(mensagem: String, traceId: String, t: Throwable, causeBy: String? = t.cause?.message) : this(mensagem, traceId){
        this.causeBy = causeBy?.lineSequence()?.firstOrNull { it.isNotBlank() }?.trim()
    }
}
