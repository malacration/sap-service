package br.andrew.sap.controllers.documents

import br.andrew.sap.model.PrazoPagamentoDto
import br.andrew.sap.services.PrazoPagamentoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("prazo")
class PrazoPagamentoController(val prazoPagamentoService: PrazoPagamentoService) {

    @GetMapping("tabela/{idTabela}")
    fun getByTabela(@PathVariable idTabela : Int): List<PrazoPagamentoDto>? {
        return prazoPagamentoService.getByTabela(idTabela)
    }
}

