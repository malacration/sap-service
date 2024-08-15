package br.andrew.sap.services.journal

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.enums.Cancelled.tNO as tNO
import br.andrew.sap.model.enums.Cancelled.tYES as tYES

interface EntryOriginalJournal{


    fun getMemoForJournal() : String

    fun getDefaultForJournal(doc: Document, type : String): String {
        return if(doc.Cancelled == tNO)
            "$type - ${doc.SequenceSerial} - ${doc.CardCode} - ${doc.cardName}"
        else if(doc.Cancelled == tYES)
            "$type - ${doc.SequenceSerial} - ${doc.CardCode} - ${doc.cardName} (Cancelado)"
        else
            "$type - ${doc.SequenceSerial} - ${doc.CardCode} - ${doc.cardName} (Cancelamento)"
    }

}
