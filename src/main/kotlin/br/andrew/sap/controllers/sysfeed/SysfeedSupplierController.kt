package br.andrew.sap.controllers.sysfeed

import br.andrew.sap.services.sysfeed.SysfeedSupplierExecutionResult
import br.andrew.sap.services.sysfeed.SysfeedSupplierLineResult
import br.andrew.sap.services.sysfeed.SysfeedSupplierService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sysfeed/fornecedores")
class SysfeedSupplierController(
    private val service: SysfeedSupplierService
) {
    @PostMapping("executar")
    fun executePending(): SysfeedSupplierExecutionResult = service.executePending()

    @PostMapping("executar/{cardCode}")
    fun executeByCardCode(@PathVariable cardCode: String): SysfeedSupplierLineResult {
        return service.executeByCardCode(cardCode)
    }

    @GetMapping("{identifier}")
    fun getSupplier(@PathVariable identifier: String): String = service.getSupplier(identifier)

    @GetMapping
    fun getSuppliers(): String = service.getSuppliers()
}
