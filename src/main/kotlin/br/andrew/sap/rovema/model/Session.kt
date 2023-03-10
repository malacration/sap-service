package br.andrew.sap.rovema.model

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
class Session(val sessionId : String, val version : String, val sessionTimeout : String) {

    override fun toString(): String {
        return sessionId
    }

}