package br.andrew.sap.controllers.handler

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class ErroDto(val mensagem : String, val traceId : String) {

    var stackTrace : String? = null
    constructor(mensagem: String, traceId: String, t: Throwable) : this(mensagem, traceId){
        this.stackTrace = t.stackTraceToString()
    }
}

