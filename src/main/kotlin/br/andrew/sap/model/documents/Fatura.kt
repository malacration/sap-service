package br.andrew.sap.model.documents

import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.documents.base.Installment

class Fatura(private val documento : Document, var boletosIds : List<String> = listOf()) {

    val id = documento.docNum
    val docEntry = documento.docEntry
    val nota = documento.SequenceSerial
    val data = documento.docDate
    val valor = documento.DocTotal
    val vencimentoUltimaParcela = documento.DocDueDate
    val vencimentoProximaParcela = documento.DocDueDate
    val isBoleto = boletosIds.any { it == documento.paymentMethod }
    val parcelas : List<Parcela>? = documento.documentInstallments?.map {
        Parcela(it,isBoleto) }
}

class Parcela(installment: Installment, val isBoleto : Boolean) {

    val id : String? = installment.InstallmentId?.toString()
    val vencimento : String? = installment.dueDate
    val valor : Double = installment.total

}