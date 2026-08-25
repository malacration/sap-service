package br.andrew.sap.controllers.pricing

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.sistema.Comissao
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.pricing.PriceListsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("comissao")
class ComissaoController(val service: ComissaoService) {

    @GetMapping()
    fun get() : List<Comissao> {
        return service.getTodas()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : String) : Comissao {
        return service.get(id)
    }

    @GetMapping("/tabela/{id}")
    fun getByIdTabela(@PathVariable id : Int) : Comissao {
        return service.getByIdTabela(id)
    }

    @PostMapping()
    fun criar(@RequestBody comissao : Comissao) : Comissao {
        return service.criar(comissao)
    }

    @PatchMapping("{id}")
    fun atualizar(@PathVariable id : String, @RequestBody comissao : Comissao) : Comissao {
        return service.atualizar(id, comissao)
    }
}
