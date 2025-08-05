package br.andrew.sap.controllers.stock

import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.stock.ItemsService
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
    fun fullSearchText(@RequestBody keyword : String, auth : Authentication,  @PathVariable branchId : Int): ResponseEntity<NextLink<Product>>? {
        return if(auth is User)
            ResponseEntity.ok(service.fullSearchText(keyword,auth.getIdInt(), branchId))
        else
            ResponseEntity.noContent().build()
    }



}