package br.andrew.sap.model.sap.documents.base.adiantamento

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class DownPaymentsToDraw {
    var docEntry: Int? = null
    var postingDate: String? = null
    var dueDate: String? = null
    var name: String? = null
    var details: String? = null
    var amountToDraw: Double? = null
    var downPaymentType: String? = null
    var amountToDrawFC: Double? = null
    var amountToDrawSC: Double? = null
    var docInternalID: Int? = null
    var rowNum: Int? = null
    var docNumber: Int? = null
    var tax: Double? = null
    var taxFC: Double? = null
    var taxSC: Double? = null
    var grossAmountToDraw: BigDecimal? = null
    var grossAmountToDrawFC: Double? = null
    var grossAmountToDrawSC: Double? = null
    var isGrossLine: String? = null
    var downPaymentsToDrawDetails: List<DownPaymentDetail>? = null
}