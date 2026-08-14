package br.andrew.sap.controllers.documents

import br.andrew.sap.model.sap.documents.CreditNotes
import br.andrew.sap.services.documents.CreditNotesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("credit-notes")
class CreditNotesController(val service : CreditNotesService) {

    @GetMapping("")
    fun get(page : Pageable): Page<CreditNotes> {
        return service.get(page).tryGetPageValues(page)
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : Int): Any {
        return service.getById(id)
    }
}
