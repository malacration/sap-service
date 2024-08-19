package br.andrew.sap.model.payment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class PaymentTermsLines(
    val CTGCode: Int,
    val InstMonth: Int,
    val InstDays: Int,
    val InstPrcnt: String
){
    override fun toString(): String {
        return "porcentagem: $InstPrcnt"
    }
}