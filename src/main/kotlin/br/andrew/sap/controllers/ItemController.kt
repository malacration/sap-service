package br.andrew.sap.controllers

import br.andrew.sap.services.ItemsService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("item")
class ItemController(val service: ItemsService) {


    @GetMapping()
    fun get() : Any{
        return service.get()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String) : Any{
        return service.getById("'$id'")
    }

    @PostMapping("search/branch/{branchId}")
    fun fullSearchText(@RequestBody keyword : String, @PathVariable branchId : Int) : Any {
        //TODO modificar Id do vendedor
        return service.fullSearchText(keyword,27)
    }

}