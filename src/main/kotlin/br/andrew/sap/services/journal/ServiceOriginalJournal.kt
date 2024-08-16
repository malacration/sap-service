package br.andrew.sap.services.journal

import br.andrew.sap.model.sap.journal.OriginalJournal

interface ServiceOriginalJournal{

    fun getEntryOriginalJournal(idJournal : Int) : EntryOriginalJournal

    fun getOriginalJournal() : OriginalJournal
}