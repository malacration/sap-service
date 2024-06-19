package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.Version
import br.andrew.sap.services.abstracts.TesteService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class IndexController(val version : Version, val teste : TesteService){

    val logger = LoggerFactory.getLogger(IndexController::class.java)

    @GetMapping("/")
    fun index() : Version{
        return version
    }

    @GetMapping("/teste")
    fun teste() : OData {
        return teste.get()
    }



    @PostMapping("/logar")
    fun forCorsOrigin() {
        println("EndPoint for optionals login")
    }

}
