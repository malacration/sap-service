package br.andrew.sap.model.sap.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Person(){
        var FirstName : String? = null
        var Name : String? = null

        @JsonProperty("DateOfBirth")
        var DateOfBirth : String? = null

        var U_TX_IdFiscalAut : String? = null
        var U_tipoPessoa : String? = null
        var CardCode : String? = null
        var InternalCode : Int? = null
}