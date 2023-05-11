package br.andrew.sap.model

import br.andrew.sap.model.documents.Product
import java.math.BigDecimal
import java.math.RoundingMode

class PrecoUnitarioComDesoneracao {

    fun calculaPreco(produto : Product, tax : SalesTaxAuthorities) : BigDecimal{
        if(produto.U_preco_negociado == null)
            throw Exception("Não é possivel calcular desonerado de um valor null")
        return calculaPreco(BigDecimal(produto.U_preco_negociado!!),tax,BigDecimal(produto.discountPercent?:0.0))
    }

    fun calculaPreco(valorAlvo : BigDecimal, tax : SalesTaxAuthorities, discountPercent : BigDecimal = BigDecimal("0")) : BigDecimal{
        val sem = BigDecimal(100)
        val one = BigDecimal(1)
        val rate = one
                .minus(BigDecimal(tax.Rate).divide(sem))
        val minusDiscont = one.minus(discountPercent.divide(sem))
        val base = BigDecimal(tax.u_Outros).divide(sem)
        return if(base > BigDecimal(0.0))
            base.multiply(valorAlvo)
                    .divide(minusDiscont,4,RoundingMode.UP)
                    .divide(rate,4, RoundingMode.UP)
        else
            valorAlvo
    }
}