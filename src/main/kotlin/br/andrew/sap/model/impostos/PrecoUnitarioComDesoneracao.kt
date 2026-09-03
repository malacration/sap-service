package br.andrew.sap.model.impostos

import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.model.sap.documents.base.DocumentLines
import java.math.BigDecimal
import java.math.RoundingMode

class PrecoUnitarioComDesoneracao {

    fun calculaPreco(produto : DocumentLines, tax : SalesTaxAuthorities) : BigDecimal{
        return calculaPreco(produto.precoAlvo(),tax,BigDecimal(produto.DiscountPercent?:0.0))
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

    /**
     * Mesma majoracao, recebendo a alíquota pronta em vez de uma autoridade fiscal.
     *
     * Existe para o frete, onde a alíquota pode ser a media ponderada de varios rateios com
     * codigos de imposto diferentes - nesse caso nao ha uma `SalesTaxAuthorities` unica que
     * represente o documento. Com um codigo so o resultado e identico ao [calculaPreco].
     *
     * Alíquota zero devolve o proprio valor: sem imposto a compensar, nao ha o que majorar.
     */
    fun calculaPrecoComTaxa(valorAlvo : BigDecimal, taxa : BigDecimal) : BigDecimal{
        if(taxa.signum() <= 0)
            return valorAlvo
        return valorAlvo.divide(BigDecimal(1).minus(taxa), 4, RoundingMode.HALF_DOWN)
    }
}