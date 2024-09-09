package br.andrew.sap.controllers.documents

import br.andrew.sap.model.PrazoPagamentoDto
import br.andrew.sap.model.payment.PaymentMethodDto
import br.andrew.sap.services.FormaPagamentoService
import br.andrew.sap.services.PrazoPagamentoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("forma-pagamento")
class FormaPagamentoController(val service: FormaPagamentoService) {

    @GetMapping("{filial}")
    fun getByFilial(@PathVariable filial : Int): List<PaymentMethodDto>? {
        return service.getByFilial(filial)
    }
}

