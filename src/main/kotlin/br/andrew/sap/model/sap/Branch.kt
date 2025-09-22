package br.andrew.sap.model.sap

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Branch(
    @JsonProperty("BPLID")
    val BPLId : Int,
    @JsonProperty("BPLName")
    val BPLName : String
){

    @JsonProperty("PrefState")
    var PrefState: String? = null
}