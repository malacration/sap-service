package br.andrew.sap.controllers.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedReceivingOrderRequest
import br.andrew.sap.services.sysfeed.SysfeedReceivingExecutionResult
import br.andrew.sap.services.sysfeed.SysfeedReceivingOrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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

    @GetMapping("{docEntry}")
    fun getByDocEntry(@PathVariable docEntry: Int): List<SysfeedReceivingOrderRequest> {
        return service.getPayloadsByDocEntry(docEntry)
    }

    @PostMapping("executar")
    fun executePending(): SysfeedReceivingExecutionResult {
        return service.executePending()
    }

    @PostMapping("executar/{docEntry}")
    fun executeByDocEntry(@PathVariable docEntry: Int): SysfeedReceivingExecutionResult {
        return service.executeByDocEntry(docEntry)
    }
}
