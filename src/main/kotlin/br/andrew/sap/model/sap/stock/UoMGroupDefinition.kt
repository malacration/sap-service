package br.andrew.sap.model.sap.stock

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class UoMGroupDefinition {
    var AlternateUoM: Int? = null
    var AlternateQuantity: Double? = null
    var BaseQuantity: Double? = null
    var WeightFactor: Int? = null
    var UdfFactor: Int? = null
    var Active: String? = null
}