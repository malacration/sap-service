package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedConfig
import br.andrew.sap.model.sysfeed.SysfeedReceivingConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SysfeedConfigService(
    @Value("\${sysfeed.recebimento.start-date:2026-01-01}") startDate: String,
    @Value("\${sysfeed.recebimento.usage:15}") usage: Int
) {
    @Volatile
    private var config = SysfeedConfig(SysfeedReceivingConfig(startDate, usage))

    fun get(): SysfeedConfig = config

    fun update(newConfig: SysfeedConfig): SysfeedConfig {
        LocalDate.parse(newConfig.recebimento.startDate)
        require(newConfig.recebimento.usage > 0) { "usage deve ser maior que zero" }
        config = newConfig
        return config
    }
}
