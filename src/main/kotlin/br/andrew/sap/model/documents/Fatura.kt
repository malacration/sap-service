package br.andrew.sap.model.documents

import br.andrew.sap.model.documents.base.Document

class Fatura(private val documento : Document) {

    val id = documento.docNum
    val data = documento.docDate
    val valor = documento.DocTotal
    val parcelas = documento.documentInstallments?.size
    val vencimentoUltimaParcela = documento.DocDueDate
    val vencimentoProximaParcela = documento.DocDueDate
}