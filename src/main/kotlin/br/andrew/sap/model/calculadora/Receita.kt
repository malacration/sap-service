package br.andrew.sap.model.calculadora

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Receita(
    val treeCode : String,
    val quantity : Double,
    val ProductDescription : String,
    val ProductTreeLines : MutableList<ReceitaLinhas>
) {
    fun qtdMaterial(itemCode : String): BigDecimal {
        return ProductTreeLines.filter { it.ItemCode == itemCode }.firstOrNull()?.Quantity ?: BigDecimal.ZERO
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ReceitaLinhas(
    val ItemCode : String,
    val Quantity : BigDecimal,
    val ItemName : String,
    val Warehouse : String


){

}