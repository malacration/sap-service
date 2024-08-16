package br.andrew.sap.services.journal

import org.springframework.stereotype.Service

@Service
class JournalMemoHandle(
    val journalEntryService: JournalEntriesService,
    val services: List<ServiceOriginalJournal>){

    fun updateMemoJournal(idJournal: Int) {
        val journalEntry = journalEntryService.getByDocEntry(idJournal)

        journalEntry?.Original?.let { original ->
            val service = services.firstOrNull {
                it.getOriginalJournal().toString() == journalEntry.OriginalJournal
            }
            if (service != null) {
                val memo = service.getEntryOriginalJournal(original).getMemoForJournal()
                journalEntryService.updateMemoJournalEntry(idJournal, memo)
            }else{
                journalEntryService.markMemoChecked(idJournal)
            }

        }
    }

}