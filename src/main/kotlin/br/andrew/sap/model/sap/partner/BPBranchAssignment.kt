package br.andrew.sap.model.sap.partner

import br.andrew.sap.model.enums.Cancelled
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class BPBranchAssignment {

    //Sem o nome explicito o Jackson tira a maiuscula do getter ("disabledForBP", "bpcode") e o
    //Service Layer recusa: "Property 'disabledForBP' of 'BPBranchAssignmentItem' is invalid"
    @get:JsonProperty("BPCode") @set:JsonProperty("BPCode")
    var BPCode : String? = null
    @JsonProperty("BPLID")
    var bplid : String? = null
    @get:JsonProperty("DisabledForBP") @set:JsonProperty("DisabledForBP")
    var DisabledForBP : Cancelled? = null
}