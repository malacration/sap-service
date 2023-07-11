package br.andrew.sap.model.partner

import br.andrew.sap.model.Cancelled
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class BPBranchAssignment {

    var BPCode : String? = null
    var BPLID : String? = null
    var DisabledForBP : Cancelled? = null
}