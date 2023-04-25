package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ApprovalRequests {
    var code : Int? = null
    var draftEntry : Int? = null;
    var approvalRequestDecisions : List<Decision> = listOf()
    var status : String? = null
    var U_starvation: String? = null
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Decision(val status : String = "ardApproved", val remarks : String = "Approved - OK")