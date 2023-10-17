package br.andrew.sap.model.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ReferenciaComercial() {
    var Code : String? = null
    var U_cardCode : String? = null

    constructor(cardCode : String) : this() {
        this.U_cardCode = cardCode
        this.Code = cardCode
    }

    private var REFERENCIACollection : List<Referencias>? = listOf()

    @JsonProperty("REFERENCIACollection")
    fun getReferencias() : List<Referencias>? {
        return this.REFERENCIACollection
    }

    fun setReferencias(referencias : List<Referencias>) {
        this.REFERENCIACollection = referencias
    }
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Referencias() {
    var Code : String? = null
    var LineId : Int? = null
    var U_nome : String? = null
    var U_telefone : String? = null
    var U_anotacao : String? = null
}