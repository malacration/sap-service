package br.andrew.sap.model.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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