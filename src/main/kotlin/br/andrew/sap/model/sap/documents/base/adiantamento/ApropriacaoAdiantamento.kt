package br.andrew.sap.model.sap.documents.base.adiantamento

import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import java.math.BigDecimal

class ApropriacaoAdiantamento(val invoice : Invoice, val adiantamentos : List<DownPayment>){

    fun get() : List<DownPaymentsToDraw>{
        var totalNecessario = BigDecimal(invoice.DocTotal ?: "0")
        var resultado = mutableListOf<DownPaymentsToDraw>()
        adiantamentos.filter { it.adiantamentoDisponivel().compareTo(BigDecimal.ZERO) > 0}
            .forEach { adiantamento ->
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
            if(totalNecessario.compareTo(BigDecimal.ZERO) == 0)
                return@forEach
        }
        return if(totalNecessario.compareTo(BigDecimal.ZERO) == 0)
            resultado
        else
            listOf()
    }
}