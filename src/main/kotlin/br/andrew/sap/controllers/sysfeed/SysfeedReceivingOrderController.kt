package br.andrew.sap.controllers.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedReceivingOrderRequest
import br.andrew.sap.services.sysfeed.SysfeedReceivingOrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sysfeed/ordens-recebimento")
class SysfeedReceivingOrderController(
    private val service: SysfeedReceivingOrderService
) {
    @GetMapping("pendentes")
    fun getPending(): List<SysfeedReceivingOrderRequest> {
        return service.getPendingPayloads()
    }
}
