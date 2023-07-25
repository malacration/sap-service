package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.services.ItemsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("item")
class ItemController(
        val service: ItemsService) {


    @GetMapping()
    fun get() : Any{
        return service.get()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String) : Any{
        return service.getById("'$id'")
    }

}