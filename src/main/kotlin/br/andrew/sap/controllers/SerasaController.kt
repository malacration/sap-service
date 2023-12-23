package br.andrew.sap.controllers

import br.andrew.sap.services.SerasaService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("serasa")
class SerasaController(val service : SerasaService) {

    @GetMapping("{cpf}")
    fun getTest(@PathVariable cpf : String): Any? {
        return service.getByCpf(cpf)
    }

    @GetMapping("{cpf}/score")
    fun score(@PathVariable cpf : String): Any? {
        return service.getByCpf(cpf,true)
    }
}