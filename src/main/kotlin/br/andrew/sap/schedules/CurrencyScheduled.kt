package br.andrew.sap.schedules

import br.andrew.sap.controllers.CurrencyController
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class CurrencyScheduled(val controller : CurrencyController) {


    @Scheduled(cron = "0 0 0 * * *")
    fun atualizaRateBacen(){
        controller.atualizaRateBacen()
    }
}