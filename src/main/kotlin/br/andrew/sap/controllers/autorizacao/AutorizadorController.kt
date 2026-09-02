package br.andrew.sap.controllers.autorizacao

import br.andrew.sap.model.sistema.Autorizador
import br.andrew.sap.services.autorizacao.AutorizadorService
import br.andrew.sap.services.autorizacao.RegraAutorizacaoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("autorizador")
class AutorizadorController(val service: AutorizadorService,
                            val regraService: RegraAutorizacaoService) {

    @GetMapping("")
    fun get(): List<Autorizador> {
        return service.getTodos()
    }

    //motivos que o motor de regras produz - alimenta o select do cadastro, para nao existir
    //autorizador de motivo que nenhuma regra gera
    @GetMapping("motivos")
    fun motivos(): List<String> {
        return regraService.motivos()
    }

    @PostMapping("")
    fun criar(@RequestBody autorizador: Autorizador): Autorizador {
        return service.criar(autorizador)
    }

    //id e String: o Code do UDO e alfanumerico (ver Autorizador.Code)
    @DeleteMapping("{id}")
    fun remover(@PathVariable id: String) {
        service.remover(id)
    }
}
