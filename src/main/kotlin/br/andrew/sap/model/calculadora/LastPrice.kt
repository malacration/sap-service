package br.andrew.sap.model.calculadora

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LastPrice(
    val ItemCode : String,
    val LastPurPrc : BigDecimal = BigDecimal.ZERO,
    val LstEvlPric : BigDecimal = BigDecimal.ZERO,
    val AvgPrice : BigDecimal = BigDecimal.ZERO,
)