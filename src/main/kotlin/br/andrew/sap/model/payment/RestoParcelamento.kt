package br.andrew.sap.model.payment

import java.math.BigDecimal
import java.math.RoundingMode

class RestoParcelamento {

    fun getResto(parcela : BigDecimal) : Pair<BigDecimal,BigDecimal>{
        val truncado = parcela.setScale(2, RoundingMode.DOWN)
        val resto = parcela.subtract(truncado)
        return Pair(truncado,resto)
    }
}