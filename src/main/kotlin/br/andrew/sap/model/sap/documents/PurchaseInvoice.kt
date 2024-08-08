package br.andrew.sap.model.sap.documents

import br.andrew.sap.infrastructure.NfeModelDefaultBean
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.services.journal.EntryOriginalJournal

class PurchaseInvoice(CardCode: String,
                      DocDueDate: String?,
                      DocumentLines: List<DocumentLines>,
                      BPL_IDAssignedToInvoice: String) :
        Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice), EntryOriginalJournal {

    init {
        if(model==null)
            model = NfeModelDefaultBean.model
    }

    fun duplicate() : PurchaseInvoice {
        val produtos = DocumentLines.map { it.Duplicate() }
        return PurchaseInvoice(CardCode,DocDueDate,produtos, getBPL_IDAssignedToInvoice()).also {
            it.model = model
            it.documentInstallments = this.documentInstallments
            it.journalMemo = this.journalMemo
            it.docDate = this.docDate
            it.controlAccount = this.controlAccount
        }
    }

    override fun toString(): String {
        return "OrderSales(CardCode='$CardCode', Branch='${getBPL_IDAssignedToInvoice()}', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }

    override fun getMemoForJournal() : String{
        return getDefaultForJournal(this,"Nota fiscal de Entrada")
    }
}