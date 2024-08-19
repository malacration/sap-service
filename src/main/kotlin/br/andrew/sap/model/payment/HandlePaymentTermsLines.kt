package br.andrew.sap.model.payment

import br.andrew.sap.model.self.vendafutura.Contrato
import java.math.BigDecimal
import java.time.LocalDate

class HandlePaymentTermsLines(val paymenetLines: List<PaymentTermsLines>) {

    init {
        if(paymenetLines.size != 0 && paymenetLines.map { BigDecimal(it.InstPrcnt) }.sumOf { it }.compareTo(BigDecimal(100)) != 0 )
            throw Exception("A soma das porcentagens nao e igual a 100%")
    }

    fun calculaVencimentos(docTotal: String, dataInicial: LocalDate): List<PaymentDueDates> {
        if(paymenetLines.isEmpty())
            return listOf(PaymentDueDates(BigDecimal(docTotal).setScale(2), dataInicial))
        var remainingAmount = BigDecimal.ZERO
        val dueDates = paymenetLines.map {
            val percentage = BigDecimal(it.InstPrcnt).divide(BigDecimal(100))
            val valorEhResto = RestoParcelamento().getResto(percentage.multiply(BigDecimal(docTotal)))
            remainingAmount += valorEhResto.second
            PaymentDueDates(valorEhResto.first, dataInicial.plusDays(it.InstDays.toLong()).plusMonths(it.InstMonth.toLong()))
        }
        var valorAhDistribuir = RestoParcelamento().getResto(remainingAmount).first
        var index = 0
        while(valorAhDistribuir.compareTo(BigDecimal.ZERO) > 0){
            dueDates.get(index).value = dueDates.get(index).value.plus(BigDecimal("0.01"))
            valorAhDistribuir = valorAhDistribuir.minus(BigDecimal("0.01"))
            index++
            if(index >= dueDates.size)
                index = 0
        }
        return dueDates
    }

    fun calculaVencimentos(total: Contrato): List<PaymentDueDates> {
        return calculaVencimentos(total.total().toString(),LocalDate.now())
    }
}

class PaymentDueDates(var value : BigDecimal, val dueDate : LocalDate){
    override fun toString(): String {
        return "valor: $value | vencimento ${dueDate.toString()}"
    }

    constructor(value : String, dueDate : LocalDate) : this(BigDecimal(value),dueDate)

    @Deprecated("Use string ou bigdecimal")
    constructor(value : Double, dueDate : LocalDate) : this(BigDecimal(value.toString()),dueDate)
}