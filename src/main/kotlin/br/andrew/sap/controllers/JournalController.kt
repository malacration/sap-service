package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.services.JournalEntriesService
import br.andrew.sap.services.JournalEntry
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("journal")
class JournalController(val journalEntryService : JournalEntriesService) {

    @GetMapping()
    fun getRegistros(): OData {
        return journalEntryService.get(OrderBy("Number", Order.DESC))
    }

    @PostMapping("save")
    fun create(@RequestBody csv: String): List<JournalEntry> {
        return journalEntryService.readCsv(csv.byteInputStream()).map{ journalEntryService.save(it).tryGetValue<JournalEntry>()}
    }

    @GetMapping("replace/{docEntry}")
    fun replaceJournalEntryMemo(@PathVariable docEntry: Int): Boolean {
        val journalEntry = journalEntryService.getByDocEntry(docEntry)
        if (journalEntry == null) {
            throw Exception("Lançamento Contábil não encontrado")
        }

        val updatedMemo = when {
            journalEntry.OriginalJournal == "ttJournalEntry" && !journalEntry.Reference.isNullOrEmpty() -> "Entrada com referência"
            journalEntry.OriginalJournal == "ttJournalEntry" -> "Entrada"
            journalEntry.OriginalJournal == "ttAPInvoice" -> "Saída"
            else -> journalEntry.memo
        }

        journalEntry.memo = updatedMemo
        journalEntryService.updateMemoJournalEntry(journalEntry)

        return true
    }


}