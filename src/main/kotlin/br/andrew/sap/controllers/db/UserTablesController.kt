package br.andrew.sap.controllers.db

import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("user-tables")
class UserTablesController(val userTableService: UserTablesMDService) {

    @GetMapping()
    fun get(page : Pageable) : Any{
        return userTableService.get(page)
    }
}