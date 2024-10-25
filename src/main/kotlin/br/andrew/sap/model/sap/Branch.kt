package br.andrew.sap.model.sap

import com.fasterxml.jackson.annotation.JsonProperty

class Branch(val BPLId : Int, val BPLName : String){
    @JsonProperty("PrefState")
    var PrefState: String? = null
}