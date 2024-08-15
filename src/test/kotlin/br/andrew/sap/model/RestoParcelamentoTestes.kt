package br.andrew.sap.model

import br.andrew.sap.model.payment.RestoParcelamento
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class RestoParcelamentoTestes {

    @Test
    fun pegaResto(){
        val resultado = RestoParcelamento().getResto(BigDecimal("33.333"))
        Assertions.assertEquals("33.33", resultado.first.toString())
        Assertions.assertEquals("0.003", resultado.second.toString())
    }

    @Test
    fun retornoNaoTemResto(){
        val resultado = RestoParcelamento().getResto(BigDecimal("33.33"))
        Assertions.assertEquals("33.33", resultado.first.toString())
        Assertions.assertEquals("0.00", resultado.second.toString())
    }

    @Test
    fun testeprimo(){
        val resultado = RestoParcelamento().getResto(BigDecimal("177.32"))
        Assertions.assertEquals("177.32", resultado.first.toString())
        Assertions.assertEquals("0.00", resultado.second.toString())
    }
}