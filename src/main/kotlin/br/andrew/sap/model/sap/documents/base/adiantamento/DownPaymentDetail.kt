package br.andrew.sap.model.sap.documents.base.adiantamento

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class DownPaymentDetail(
    val docInternalID: Int,
    val rowNum: Int,
    val seqNum: Int,
    val docEntry: Int,
    val vatGroupCode: String?,
    val vatPercent: Double,
    val amountToDraw: Double,
    val amountToDrawFC: Double?,
    val amountToDrawSC: Double,
    val tax: Double,
    val taxFC: Double?,
    val taxSC: Double,
    val isGrossLine: String,
    val grossAmountToDraw: Double,
    val grossAmountToDrawFC: Double,
    val grossAmountToDrawSC: Double,
    val lineType: String,
    val taxAdjust: String
)