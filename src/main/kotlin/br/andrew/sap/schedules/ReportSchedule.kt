package br.andrew.sap.schedules

import br.andrew.sap.controllers.MailController
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.ApprovalRequests
import br.andrew.sap.model.User
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.*
import br.andrew.sap.services.approval.ApprovalRequestsService
import br.andrew.sap.services.approval.ApprovalStagesService
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context


@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class ReportSchedule(
    val controller : MailController) {



    @Scheduled(cron = "0 0 7 * * MON")
    fun execute() {
        controller.enviarInadimplencia()
    }
}