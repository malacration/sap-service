package br.andrew.sap.controllers


import br.andrew.sap.model.Version
import br.andrew.sap.services.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class IndexController(val version : Version) {

    @GetMapping("/")
    fun index() : Version{
        return version
    }
}