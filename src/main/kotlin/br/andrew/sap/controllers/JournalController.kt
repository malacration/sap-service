package br.andrew.sap.controllers

import JournalEntry
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.journal.JournalMemoHandle
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("journal")
class JournalController(val journalEntry : JournalEntriesService,
    val teste : JournalMemoHandle) {

    @GetMapping()
    fun getRegistros(): OData {
        return journalEntry.get(OrderBy("JdtNum", Order.DESC))
    }

    @PostMapping("save")
    fun create(@RequestBody csv: String): List<JournalEntry> {
        return journalEntry.readCsv(csv.byteInputStream()).map{
            journalEntry.save(it).tryGetValue<JournalEntry>()
        }
    }

    @GetMapping("default-memo/{docEntry}")
    fun replaceJournalEntryMemo(@PathVariable docEntry: Int): Boolean{
        teste.updateMemoJournal(docEntry)
        return true
    }

    @GetMapping("search/{docEntry}")
    fun search(@PathVariable docEntry: Int): List<JournalEntry> {
        return journalEntry.search(docEntry)
    }
}