package br.andrew.sap.model.documents

import br.andrew.sap.infrastructure.NfeModelDefaultBean

class PurchaseInvoice(CardCode: String,
                      DocDueDate: String?,
                      DocumentLines: List<DocumentLines>,
                      BPL_IDAssignedToInvoice: String) :
        Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice) {

    init {
        if(model==null)
            model = NfeModelDefaultBean.model
    }

    fun duplicate() : PurchaseInvoice{
        val produtos = DocumentLines.map { it.Duplicate() }
        return PurchaseInvoice(CardCode,DocDueDate,produtos, getBPL_IDAssignedToInvoice()).also {
            it.model = model
            it.documentInstallments = this.documentInstallments
            it.journalMemo = this.journalMemo
            it.docDate = this.docDate
            it.controlAccount = this.controlAccount
        }
    }
}