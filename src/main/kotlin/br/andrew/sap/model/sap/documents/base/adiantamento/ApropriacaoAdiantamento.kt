package br.andrew.sap.model.sap.documents.base.adiantamento

import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import java.math.BigDecimal

class ApropriacaoResultado(val downPayments: List<DownPaymentsToDraw>, val diferenca: BigDecimal)

class ApropriacaoAdiantamento(val invoice : Invoice, val adiantamentos : List<DownPayment>){

    fun calcular() : ApropriacaoResultado {
        var totalNecessario = BigDecimal(invoice.DocTotal ?: "0")
        var resultado = mutableListOf<DownPaymentsToDraw>()
        adiantamentos.filter { it.adiantamentoDisponivel().compareTo(BigDecimal.ZERO) > 0}
            .forEach { adiantamento ->
                if(totalNecessario.compareTo(BigDecimal.ZERO) == 0)
                    return@forEach
                if(adiantamento.adiantamentoDisponivel().compareTo(totalNecessario) >= 0) {
                    resultado.add(DownPaymentsToDraw().also {
                        it.docEntry = adiantamento.docEntry
                        it.grossAmountToDraw = totalNecessario
                    })
                    totalNecessario = BigDecimal.ZERO
                } else {
                    resultado.add(DownPaymentsToDraw().also {
                        it.docEntry = adiantamento.docEntry
                        it.grossAmountToDraw = adiantamento.adiantamentoDisponivel()
                    })
                    totalNecessario = totalNecessario.minus(adiantamento.adiantamentoDisponivel())
                }
        }
        // O loop acima nunca puxa mais do que totalNecessario, então o residual nunca fica negativo.
        require(totalNecessario.signum() >= 0) { "Resíduo da apropriação não pode ser negativo" }
        return ApropriacaoResultado(resultado.toList(), totalNecessario)
    }

    fun get() : List<DownPaymentsToDraw>{
        val resultado = calcular()
        return if(resultado.diferenca.compareTo(BigDecimal.ZERO) == 0)
            resultado.downPayments
        else
            listOf()
    }
}
