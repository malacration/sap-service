package br.andrew.sap.model.impostos

import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.model.sap.documents.base.DocumentLines
import java.math.BigDecimal
import java.math.RoundingMode

class PrecoUnitarioComDesoneracao {

    fun calculaPreco(produto : DocumentLines, tax : SalesTaxAuthorities) : BigDecimal{
        val valor = if(produto.U_preco_negociado == null || produto.U_preco_negociado!! <= 0.0)
            BigDecimal(produto.UnitPrice!!)
        else
            BigDecimal(produto.U_preco_negociado!!)
        return calculaPreco(valor,tax,BigDecimal(produto.DiscountPercent?:0.0))
    }

    fun calculaPreco(valorAlvo : BigDecimal, tax : SalesTaxAuthorities, discountPercent : BigDecimal = BigDecimal("0")) : BigDecimal{
        val sem = BigDecimal(100)
        val one = BigDecimal(1)
        val rate = one
            .minus(tax.rateBaseOutro())
        val minusDiscont = one.minus(discountPercent.divide(sem))
        return if(tax.u_Outros > 0)
            valorAlvo
                    .divide(minusDiscont,4,RoundingMode.HALF_DOWN)
                    .divide(rate,4, RoundingMode.HALF_DOWN)
        else
            valorAlvo
                .divide(minusDiscont,4,RoundingMode.HALF_DOWN)
    }
}