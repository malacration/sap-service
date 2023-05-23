package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.ApprovalRequests
import br.andrew.sap.model.User
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.ApprovalRequestsService
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.TelegramRequestService
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
@DisallowConcurrentExecution
class AutoApprovalPaymentCondition(
        val approvalRequestsService : ApprovalRequestsService,
        val draftsService: DraftsService,
        val currentUser : User) : Job {

    val logger: Logger = LoggerFactory.getLogger(AutoApprovalPaymentCondition::class.java)


    override fun execute(context : JobExecutionContext) {
        try{
            val predicados = listOf(
                    Predicate("OriginatorID",currentUser.internalKey, Condicao.EQUAL),
                    Predicate("ObjectType","17", Condicao.EQUAL),
                    Predicate("IsDraft","Y", Condicao.EQUAL),
                    Predicate("Status", listOf("arsApproved","arsPending"), Condicao.IN),
                    )
            var requests : OData? = null
            do {
                requests = if(requests == null)
                        approvalRequestsService.get(Filter(predicados))
                    else
                        approvalRequestsService.next(requests)
                val approval = requests.tryGetValues<ApprovalRequests>()
                approval.filter { it.draftEntry != null }
                        .map { draftsService.getById(it.draftEntry!!).tryGetValue<OrderSales>()  }
                        .forEach {
                            try{
                                approvalRequestsService.aprovaEhCria(it,approval.first{r -> r.draftEntry == it.docEntry})
                            }catch (t : Throwable){
                                logger.error("Erro ao aprovar e draft entry ${it.docEntry} num ${it.docNum}",t)
                            }
                        }
            } while (requests!!.hasNext())

        }catch (t : Throwable){
            logger.error(t.message,t)
        }

    }
}