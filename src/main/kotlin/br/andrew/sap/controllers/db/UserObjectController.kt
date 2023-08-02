package br.andrew.sap.controllers.db

import br.andrew.sap.services.structs.UserObjectMDService
import br.andrew.sap.services.structs.UserTablesMDService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("user-object")
class UserObjectController(val userObjectMDService: UserObjectMDService) {

    @GetMapping()
    fun get(page : Pageable) : Any{
        return userObjectMDService.get(page)
    }
}