package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class AdditionalExpenses(val expenseCode : Int, val LineTotal : Double){

    var distributionMethod : String = "aedm_RowTotal"
    var TaxType : String? = null;
    var LineNum : Int? = null
    var DistributionRule : String? = null
    var DistributionRule2 : String? = null

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