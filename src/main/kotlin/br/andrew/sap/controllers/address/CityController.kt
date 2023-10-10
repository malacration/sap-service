package br.andrew.sap.controllers.address

import br.andrew.sap.services.address.CityService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("city")
class CityController(val service : CityService) {

    @GetMapping("{estado}")
    fun getById(@PathVariable estado : String): Any {
        return service.get(estado)
    }
}