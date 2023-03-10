package br.andrew.sap.rovema.controllers


import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class IndexController() {

    @GetMapping("/")
    fun index() : String{
        return "up"
    }
}