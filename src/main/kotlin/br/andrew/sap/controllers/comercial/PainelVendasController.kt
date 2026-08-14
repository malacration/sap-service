package br.andrew.sap.controllers.comercial

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.comercial.VendaDetalhe
import br.andrew.sap.model.comercial.VendaMensal
import br.andrew.sap.model.comercial.VendaProduto
import br.andrew.sap.services.comercial.PainelVendasService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("painel-vendas")
class PainelVendasController(
    private val service: PainelVendasService
) {

    @GetMapping("mensal")
    fun getTotaisMensais(
        auth: Authentication,
        @RequestParam(required = false) slpCode: Int?
    ): List<VendaMensal> {
        return service.getTotaisMensais(auth as User, slpCode)
    }

    @GetMapping("mensal/{ano}/{mes}")
    fun getDetalheMes(
        auth: Authentication,
        @PathVariable ano: Int,
        @PathVariable mes: Int,
        @RequestParam(required = false) slpCode: Int?
    ): List<VendaDetalhe> {
        return service.getDetalheMes(auth as User, ano, mes, slpCode)
    }

    @GetMapping("mensal/{ano}/{mes}/produtos")
    fun getDetalheMesPorProduto(
        auth: Authentication,
        @PathVariable ano: Int,
        @PathVariable mes: Int,
        @RequestParam(required = false) slpCode: Int?
    ): List<VendaProduto> {
        return service.getDetalheMesPorProduto(auth as User, ano, mes, slpCode)
    }
}
