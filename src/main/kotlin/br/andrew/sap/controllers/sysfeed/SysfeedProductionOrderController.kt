package br.andrew.sap.controllers.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedProductionOrderRequest
import br.andrew.sap.services.sysfeed.SysfeedProductionOrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sysfeed/ordens-producao")
class SysfeedProductionOrderController(
    private val service: SysfeedProductionOrderService
) {
    @GetMapping("pendentes")
    fun getPending(
        @RequestParam dataCorte: String
    ): List<SysfeedProductionOrderRequest> {
        return service.getPendingPayloads(dataCorte)
    }
}
