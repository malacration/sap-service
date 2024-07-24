package br.andrew.sap.model.sap.tax

import br.andrew.sap.model.sap.documents.base.Product
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class SalesTaxAuthorities(val type : Int, val Rate : Double,
                          val u_Base : Double, val u_Isento : Double,
                          val u_Outros : Double) {


    fun taxValueOutros(base : Double): Double {
        return (Rate/100)*(u_Outros/100)*base
    }

    fun rateBaseOutro(): BigDecimal {
        val sem = BigDecimal(100)
        val base = BigDecimal(u_Outros).divide(sem)
        return base.multiply(BigDecimal(Rate)).divide(sem,4, RoundingMode.HALF_UP)
    }

    fun valorImposto(p : Product): BigDecimal {
        return valorImposto(p.total())
    }

    fun valorImposto(total : BigDecimal): BigDecimal {
        return total.multiply(rateBaseOutro()).setScale(2, RoundingMode.HALF_UP)
    }
}