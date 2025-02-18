package br.andrew.sap.model.sap.stock

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class UnitOfMeasurementGroups {
    var AbsEntry: Int? = null
    var Code: String? = null
    var Name: String? = null
    var BaseUoM: Int? = null
    var UoMGroupDefinitionCollection: List<UoMGroupDefinition> = listOf()

    fun convertQuantity(fromUoMId: Int, toUoMId: Int, quantity: Double): BigDecimal {
        var fromBaseFactor = BigDecimal(1.0)
        var toBaseFactor = BigDecimal(1.0)

        for (definition in UoMGroupDefinitionCollection) {
            if (definition.AlternateUoM == fromUoMId) {
                fromBaseFactor = BigDecimal(definition.BaseQuantity!!)
                    .divide(BigDecimal(definition.AlternateQuantity!!),2, RoundingMode.HALF_UP)
            }
            if (definition.AlternateUoM == toUoMId) {
                toBaseFactor = BigDecimal(definition.BaseQuantity!!)
                    .divide(BigDecimal(definition.AlternateQuantity!!),2, RoundingMode.HALF_UP)
            }
        }
        return BigDecimal(quantity).multiply(fromBaseFactor).divide(toBaseFactor,2, RoundingMode.HALF_UP)
    }

}