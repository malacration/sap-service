package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class SalesTaxAuthorities(val type : Int, val Rate : Double,
                          val u_Base : Double, val u_Isento : Double,
                          val u_Outros : Double) {


    fun taxValueOutros(base : Double): Double {
        return (Rate/100)*(u_Outros/100)*base
    }
}