package br.andrew.sap.controllers.documents

import br.andrew.sap.model.self.vendafutura.BoletoVf
import br.andrew.sap.services.document.DownPaymentService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("down-payment")
class DownPaymentController(val service : DownPaymentService) {


    @GetMapping("contrato-venda-futura/{id}")
    fun getByContrato(@PathVariable id : Int): List<BoletoVf> {
        return service.getByContratoVendaFuturaStatus(id)
    }
}