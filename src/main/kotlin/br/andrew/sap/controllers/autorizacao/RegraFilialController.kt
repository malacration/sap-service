package br.andrew.sap.controllers.autorizacao

import br.andrew.sap.model.sistema.RegraFilial
import br.andrew.sap.services.autorizacao.RegraAutorizacaoService
import br.andrew.sap.services.autorizacao.RegraFilialService
import org.springframework.web.bind.annotation.*

/**
 * Cadastro "em que filiais cada regra esta ativa".
 *
 * O controller e quem junta os dois lados: le os motivos do motor de regras e entrega ao
 * RegraFilialService na hora de validar. O servico nao injeta RegraAutorizacaoService
 * porque o caminho inverso ja existe (o motor consulta as filiais) e fecharia um ciclo.
 */
@RestController
@RequestMapping("regra-filial")
class RegraFilialController(val service: RegraFilialService,
                            val regraService: RegraAutorizacaoService) {

    @GetMapping("")
    fun get(): List<RegraFilial> {
        return service.getTodos()
    }

    @PostMapping("")
    fun criar(@RequestBody regraFilial: RegraFilial): RegraFilial {
        return service.criar(regraFilial, regraService.motivos())
    }

    //id e String: o Code do UDO e alfanumerico (ver RegraFilial.Code)
    @DeleteMapping("{id}")
    fun remover(@PathVariable id: String) {
        service.remover(id)
    }
}
