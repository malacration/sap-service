package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class AdditionalExpenses(val expenseCode : Int, val LineTotal : Double){

    val distributionMethod : String = "aedm_RowTotal"
//    val lineTotal : Double = lineTotalSys
//    val LineGross : Double = lineTotalSys
//    val LineGrossSys : Double = lineTotalSys

    companion object{
        @JsonIgnore
        fun frete(valor : Double) : AdditionalExpenses{
            return AdditionalExpenses(1,valor)
        }
    }
}