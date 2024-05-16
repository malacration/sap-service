package br.andrew.sap.model.partner

import br.andrew.sap.model.enums.Cancelled
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class BPBranchAssignment {

    var BPCode : String? = null
    @JsonProperty("BPLID")
    var bplid : String? = null
    var DisabledForBP : Cancelled? = null


}