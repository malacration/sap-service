package br.andrew.sap.model.transaction

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class TransactionCode(val Code : String, val Description : String? = null) {

    init {
        if(Code.length > 4)
            throw Exception("Code nao pode ser maior que 4 caracteres")
        if((Description?.length ?: 0) > 20)
            throw Exception("Descricao nao pode ser maior que 20")
    }
}