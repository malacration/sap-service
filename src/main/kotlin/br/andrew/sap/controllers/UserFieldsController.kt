package br.andrew.sap.controllers

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
        val filter = Filter(listOf(Predicate("Name","B1SYS_IV",Condicao.STARTS_WITH)))
        return userFieldsMDService.get(filter,page);
    }
}