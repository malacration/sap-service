package br.andrew.sap.controllers.autorizacao

import br.andrew.sap.model.sistema.Autorizador
import br.andrew.sap.services.autorizacao.AutorizadorService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("autorizador")
class AutorizadorController(val service: AutorizadorService) {

    @GetMapping("")
    fun get(): List<Autorizador> {
        return service.getTodos()
    }

    @PostMapping("")
    fun criar(@RequestBody autorizador: Autorizador): Autorizador {
        return service.criar(autorizador)
    }

    @DeleteMapping("{id}")
    fun remover(@PathVariable id: Int) {
        service.remover(id)
    }
}
