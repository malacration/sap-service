package br.andrew.sap.controllers

import br.andrew.sap.services.UserFieldsMDService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("user-fields")
class UserFieldsController(val userFieldsMDService: UserFieldsMDService) {

    @GetMapping()
    fun get(page : Pageable) : Any{
        return userFieldsMDService.get(page);
    }
}