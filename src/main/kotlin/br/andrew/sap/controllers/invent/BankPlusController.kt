package br.andrew.sap.controllers.invent

import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.invent.OrigemBoletoEnum
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("bank-plus")
class BankPlusController(val bankPlusService : BankPlusService) {



    @GetMapping()
    fun getRegistros(): Any? {
        return bankPlusService.getBoletosBy("2","16797")
    }

    @GetMapping("empresas")
    fun empresas(): Any? {
        return bankPlusService.getEmpresas()
    }
}