package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ApprovalRequests {
    var code : Int? = null
    var draftEntry : Int? = null
    var approvalRequestDecisions : List<Decision> = listOf()
    var ApprovalRequestLines  : List<Decision> = listOf()
    var status : String? = null
    var U_starvation: String? = null

    override fun toString(): String {
        return "Code: $code - DraftEntry: $draftEntry - Status: $status"
    }

    fun isReprovado(currentuser : Int) : Boolean {
        return (this.ApprovalRequestLines
            .any { it.UserID == currentuser && it.status == "ardNotApproved"} || this.status == "ardApproved")
    }

    companion object {

        fun aprova(): ApprovalRequests {
            return ApprovalRequests().also {
                it.approvalRequestDecisions = listOf(Decision("ardApproved","Aprovado"))
            }
        }
        fun reprova(): ApprovalRequests {
            return ApprovalRequests().also {
                it.approvalRequestDecisions = listOf(Decision("ardNotApproved", "Reprovado"))
            }
        }
    }
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Decision(val status : String? = "ardApproved", val remarks : String? = null){
    var UserID: Int? = null

    override fun toString(): String {
        return "$UserID - $status - $remarks"
    }
}