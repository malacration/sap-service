package br.andrew.sap.model.calculadora

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Resource(
    val Code : String,
    val Name : String
    ) {

    var Cost1 : BigDecimal = BigDecimal.ZERO
    var Cost2 : BigDecimal = BigDecimal.ZERO
    var Cost3 : BigDecimal = BigDecimal.ZERO
    var Cost4 : BigDecimal = BigDecimal.ZERO
    var Cost5 : BigDecimal = BigDecimal.ZERO
    var Cost6 : BigDecimal = BigDecimal.ZERO
    var Cost7 : BigDecimal = BigDecimal.ZERO
    var Cost8 : BigDecimal = BigDecimal.ZERO
    var Cost9 : BigDecimal = BigDecimal.ZERO
    var Cost10 : BigDecimal = BigDecimal.ZERO

    fun sumCost(): BigDecimal {
        return Cost1.add(Cost2)
            .add(Cost3)
            .add(Cost4)
            .add(Cost5)
            .add(Cost6)
            .add(Cost7)
            .add(Cost8)
            .add(Cost9)
            .add(Cost10)
    }
}