package br.andrew.sap.controllers.comercial

import br.andrew.sap.services.comercial.NegociacaoContrato
import br.andrew.sap.services.comercial.NegociacaoContratoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** Separado do ContratoVendaFuturaController so para nao inflar mais o construtor dele; mesmo caminho base. */
@RestController
@RequestMapping("contrato-venda-futura")
class NegociacaoContratoController(val service: NegociacaoContratoService) {

    /** Forma de pagamento e parcelamento para a minuta do contrato. */
    @GetMapping("/{docEntry}/negociacao")
    fun negociacao(@PathVariable docEntry: Int): NegociacaoContrato {
        return service.negociacao(docEntry)
    }
}
