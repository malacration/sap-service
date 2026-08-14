package br.andrew.sap.controllers.documents

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.self.vendafutura.BoletoVf
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.documents.DownPaymentService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("down-payment")
class DownPaymentController(
    val service : DownPaymentService,
    @Value("\${pix.juros.mora.percent:0}") val jurosMoraPercent: Double
) {

    @GetMapping("")
    fun get(page : Pageable): Page<Document> {
        return service.get(page).tryGetPageValues(page)
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id : Int): Any {
        return service.getById(id)
    }

    @GetMapping("contrato-venda-futura/{id}")
    fun getByContrato(@PathVariable id : Int): List<BoletoVf> {
        return service.getByContratoVendaFuturaStatus(id)
    }

    @PostMapping("contrato-venda-futura/{id}/pix")
    fun gerarPixByContrato(
        @PathVariable id : Int,
        @RequestParam("juros", defaultValue = "true") juros: Boolean = true,
        auth : Authentication
    ): List<BoletoVf> {
        if(auth !is User)
            throw Exception("Não foi possivel fazer a conversão de auth para User")
        if(!auth.isAllCreatePix(juros))
            throw Exception("Não é permitido criar pix!")
        return service.createPixByContratoVendaFutura(id, if(juros) jurosMoraPercent else 0.0)
    }
}
