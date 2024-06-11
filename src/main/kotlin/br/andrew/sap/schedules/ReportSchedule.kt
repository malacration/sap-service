package br.andrew.sap.schedules

import br.andrew.sap.controllers.MailController
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class ReportSchedule(
    val controller : MailController) {



    @Scheduled(cron = "0 0 7 * * MON")
    fun execute() {
        controller.enviarInadimplencia()
    }
}