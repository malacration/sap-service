package br.andrew.sap.schedules

import br.andrew.sap.model.SapUser
import br.andrew.sap.services.approval.ApprovalRequestsService
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.document.InvoiceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class GeneratePix(
    val invoiceService: InvoiceService,
    val approvalRequestsService : ApprovalRequestsService,
    val draftsService: DraftsService,
    val currentSapUser : SapUser) {

    val logger: Logger = LoggerFactory.getLogger(GeneratePix::class.java)

    @Scheduled(fixedDelay = 15000)
    fun execute() {
        try{
            invoiceService.pendenteGerarPix().forEach {
                invoiceService.createPix(it)
            }
        }catch (t : Throwable){
            logger.error(t.message,t)
        }

    }
}