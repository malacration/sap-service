package br.andrew.sap.controllers.db

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("user-fields")
class UserFieldsController(val userFieldsMDService: UserFieldsMDService) {

    @GetMapping()
    fun get(page : Pageable) : Any{
        val filter = Filter(

        )
        return userFieldsMDService.get(filter,page)
    }

    @GetMapping("/{name}")
    fun get(name : String) : Any{
        val filter = Filter(Predicate("Name",name,Condicao.CONTAINS))
        return userFieldsMDService.get(filter)
    }
}