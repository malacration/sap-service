package br.andrew.sap.services.approval

import br.andrew.sap.model.ApprovalRequests
import br.andrew.sap.model.Decision
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class ApprovalRequestsService(env : SapEnvrioment,
                              restTemplate: RestTemplate,
                              authService: AuthService,
                              val draftsService: DraftsService
) : EntitiesService<ApprovalRequests>(env, restTemplate,authService) {
    override fun path(): String {
        return "/b1s/v1/ApprovalRequests"
    }

    fun aprovaEhCria(draft : Document, approvalRequest : ApprovalRequests) {
        if(draft.docEntry!=approvalRequest.draftEntry)
            throw Exception("O draft deve ter o mesmo id da requisição de aprovação")
        val draft = draftsService.getById(approvalRequest.draftEntry!!).tryGetValue<OrderSales>()
        if(draft.isAvista() && approvalRequest.status == "arsPending"){
            val permite = ApprovalRequests().also {
                it.approvalRequestDecisions = listOf(Decision())
            }
            update(permite, approvalRequest.code.toString())
            draftsService.draftToDocument(draft)
        }else if(approvalRequest.status == "arsPending"){
            val reprova = ApprovalRequests().also {
                it.approvalRequestDecisions = listOf(Decision("ardNotApproved"))
            }
            update(reprova, approvalRequest.code.toString())
        }else{
            draftsService.draftToDocument(draft)
        }
    }


}