package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.services.JournalEntriesService
import br.andrew.sap.services.JournalEntry
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("journal")
class JournalController(val journalEntry : JournalEntriesService) {

    @GetMapping()
    fun getRegistros(): OData {
        return journalEntry.get(OrderBy("JdtNum", Order.DESC))
    }

    @PostMapping("save")
    fun create(@RequestBody csv: String): List<JournalEntry> {
        return journalEntry.readCsv(csv.byteInputStream()).map{ journalEntry.save(it).tryGetValue<JournalEntry>()}
    }

}