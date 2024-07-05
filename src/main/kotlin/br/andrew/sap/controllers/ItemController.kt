package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.documents.base.Product
import br.andrew.sap.services.ItemsService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
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
    fun fullSearchText(@RequestBody keyword : String, auth : Authentication): ResponseEntity<NextLink<Product>>? {
        return if(auth is User)
            ResponseEntity.ok(service.fullSearchText(keyword,auth.id.toIntOrNull() ?: throw Exception("Id do vendedor nao é um int")))
        else
            ResponseEntity.noContent().build()
    }

}