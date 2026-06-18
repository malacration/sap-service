package br.andrew.sap.controllers.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedSupplierRequest
import br.andrew.sap.services.sysfeed.SysfeedSupplierService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sysfeed/fornecedores")
class SysfeedSupplierController(
    private val service: SysfeedSupplierService
) {
    @GetMapping("pendentes")
    fun getPending(): List<SysfeedSupplierRequest> = service.getPendingPayloads()
}
