package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.ApprovalRequests
import br.andrew.sap.model.User
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.approval.ApprovalRequestsService
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.TelegramRequestService
import br.andrew.sap.services.approval.ApprovalStagesService
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class AutoApprovalPaymentCondition(
    val approvalRequestsService : ApprovalRequestsService,
    val telegramRequestService: TelegramRequestService,
    val currentUser : User) {

    val logger: Logger = LoggerFactory.getLogger(AutoApprovalPaymentCondition::class.java)


    @Scheduled(fixedDelay = 300000)
    fun execute() {
        try {
            val predicados = listOf(
                Predicate("OriginatorID", currentUser.internalKey, Condicao.EQUAL),
                Predicate("ObjectType", "17", Condicao.EQUAL),
                Predicate("IsDraft", "Y", Condicao.EQUAL),
                Predicate("CreationDate", "2023-08-07", Condicao.GREAT),
                Predicate("Status", listOf("arsApproved", "arsPending"), Condicao.IN),
            )
            var requests: OData? = null
                do {
                    requests = if (requests == null)
                        approvalRequestsService.get(Filter(predicados))
                    else
                        approvalRequestsService.next(requests)
                    requests.tryGetValues<ApprovalRequests>()
                        .filter { it.draftEntry != null && !it.isReprovado(currentUser.internalKey) }
                        .forEach {
                            try {
                                approvalRequestsService.aprovaEhCria(it)
                            } catch (t: Throwable) {
                                if((t.message ?: "").contains("Enter a discount percentage of less than 100"))
                                    telegramRequestService.send("Remova o disconto da draft ${it.draftEntry} e solicite o calculo da desoneracao de ICMS!")
                                logger.error("Erro ao aprovar e draft entry ${it.draftEntry}", t)
                            }
                        }
                } while (requests!!.hasNext())
        } catch (t : Throwable){
            logger.error(t.message, t)
        }

    }
}