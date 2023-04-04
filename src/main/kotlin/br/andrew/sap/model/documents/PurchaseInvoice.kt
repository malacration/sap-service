package br.andrew.sap.model.documents

class PurchaseInvoice(CardCode: String,
                      DocDueDate: String?,
                      DocumentLines: List<Product>,
                      BPL_IDAssignedToInvoice: String) :
        Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice) {


    fun duplicate() : PurchaseInvoice{
        val produtos = DocumentLines.map { it.Duplicate() }
        return PurchaseInvoice(CardCode,DocDueDate,produtos, getBPL_IDAssignedToInvoice()).also {
            it.documentInstallments = this.documentInstallments
            it.journalMemo = this.journalMemo
            it.docDate = this.docDate
        }
    }
}