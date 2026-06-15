package br.andrew.sap.controllers.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedConfig
import br.andrew.sap.services.sysfeed.SysfeedConfigService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("sysfeed/config")
class SysfeedConfigController(
    private val service: SysfeedConfigService
) {
    @GetMapping
    fun get(): SysfeedConfig = service.get()

    @PutMapping
    fun update(@RequestBody config: SysfeedConfig): SysfeedConfig = service.update(config)
}
