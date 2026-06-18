package br.andrew.sap.controllers.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedStatusUpdate
import br.andrew.sap.model.sysfeed.SysfeedStatusUpdateResult
import br.andrew.sap.services.sysfeed.SysfeedStatusService
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sysfeed/status")
class SysfeedStatusController(
    private val service: SysfeedStatusService
) {
    @PatchMapping
    fun update(@RequestBody updates: List<SysfeedStatusUpdate>): List<SysfeedStatusUpdateResult> {
        return service.updateAll(updates)
    }

    @PostMapping
    fun updateWithPost(@RequestBody updates: List<SysfeedStatusUpdate>): List<SysfeedStatusUpdateResult> {
        return service.updateAll(updates)
    }
}
