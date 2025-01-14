package br.andrew.sap.model.sap.documents.base

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class AdditionalExpenses(val expenseCode : Int, val LineTotal : Double){

    var distributionMethod : String = "aedm_RowTotal"
    var TaxType : String? = null;
    var LineNum : Int? = null
    var DistributionRule : String? = null
    var DistributionRule2 : String? = null

//    val lineTotal : Double = lineTotalSys
//    val LineGross : Double = lineTotalSys
//    val LineGrossSys : Double = lineTotalSys

    fun setDistribuicaoCusto(custoByBranch: DistribuicaoCustoByBranch) {
        DistributionRule = custoByBranch.grupoEconomico
        DistributionRule2 = custoByBranch.centroCusto
    }

    companion object{
        @JsonIgnore
        fun frete(valor : Double) : AdditionalExpenses {
            return AdditionalExpenses(1,valor)
        }
    }
}