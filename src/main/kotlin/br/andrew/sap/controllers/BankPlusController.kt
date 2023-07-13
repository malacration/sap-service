package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.services.BankPlusService
import br.andrew.sap.services.RegistroCompraInsumoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("bank-plus")
class BankPlusController(val registroCompraService : BankPlusService) {

    @GetMapping()
    fun getRegistros(): Any? {
        return ""
    }
}