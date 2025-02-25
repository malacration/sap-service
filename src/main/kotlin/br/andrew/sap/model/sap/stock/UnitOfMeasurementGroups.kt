package br.andrew.sap.model.sap.stock

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.exp

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class UnitOfMeasurementGroups {
    var AbsEntry: Int? = null
    var Code: String? = null
    var Name: String? = null
    var BaseUoM: Int? = null
    var UoMGroupDefinitionCollection: List<UoMGroupDefinition> = listOf()

    fun convertQuantity(toUoMId: Int): BigDecimal {
        val toUom = UoMGroupDefinitionCollection
            .filter { it.AlternateUoM == toUoMId }.firstOrNull() ?: throw Exception("Unidade [$toUoMId] esperada nao cadastrada")
        val alternateQuantity = BigDecimal(toUom.AlternateQuantity)
        val baseQuantity = BigDecimal(toUom.BaseQuantity)
        return BigDecimal(1).multiply(alternateQuantity).divide(baseQuantity,2, RoundingMode.HALF_UP)
    }

}