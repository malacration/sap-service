package br.andrew.sap.model.sap

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Branch(val BPLId : Int, val BPLName : String){
    @JsonProperty("PrefState")
    var PrefState: String? = null
}