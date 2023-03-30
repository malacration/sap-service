package br.andrew.sap.model

import java.math.BigDecimal
import java.math.RoundingMode

class PrecoUnitarioComDesoneracao {

    fun calculaPreco(valorAlvo : String, tax : SalesTaxAuthorities) : BigDecimal{
        return calculaPreco(BigDecimal(valorAlvo),tax)
    }

    fun calculaPreco(valorAlvo : BigDecimal, tax : SalesTaxAuthorities) : BigDecimal{
        val rate = BigDecimal(1).minus(BigDecimal(tax.Rate).divide(BigDecimal(100)))
        val base = BigDecimal(tax.u_Outros).divide(BigDecimal(100))
        return if(base > BigDecimal(0.0))
            base.multiply(valorAlvo).divide(rate,4, RoundingMode.UP)
        else
            valorAlvo
    }
}