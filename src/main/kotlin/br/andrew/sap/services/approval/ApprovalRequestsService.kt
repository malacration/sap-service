package br.andrew.sap.services.approval

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.ApprovalRequests
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.DraftsService
import br.andrew.sap.services.TelegramRequestService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.document.DesoneradoService
import org.springframework.data.domain.Page
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class ApprovalRequestsService(env : SapEnvrioment,
                              restTemplate: RestTemplate,
                              authService: AuthService,
                              val draftsService: DraftsService,
                              val desoneradoService: DesoneradoService,
                              val telegramRequestService: TelegramRequestService
) : EntitiesService<ApprovalRequests>(env, restTemplate,authService) {
    override fun path(): String {
        return "/b1s/v1/ApprovalRequests"
    }

    fun getPendencias(): Page<ApprovalRequests>? {
//        val now = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val url = env.host+"/b1s/v1/SQLQueries"
        return restTemplate.exchange(
            RequestEntity
                .get("$url('autorizacao.sql')/List'")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build(), OData::class.java).body
            ?.tryGetPageValues() ?: Page.empty()
    }

    fun aprovaEhCria(draft : Document, approvalRequest : ApprovalRequests) {
        if(draft.u_pedido_update == "1"){
            draftsService.update(desoneradoService.aplicaDesonerado(draft),draft.docEntry.toString())
        }else if(draft.docEntry!=approvalRequest.draftEntry)
            throw Exception("O draft deve ter o mesmo id da requisição de aprovação")
        else if(draft.isAvista() && approvalRequest.status == "arsPending"){
            update(ApprovalRequests.aprova(), approvalRequest.code.toString())
        } else if(approvalRequest.status == "arsPending"){
            update(ApprovalRequests.reprova(), approvalRequest.code.toString())
        }else{
            draftsService.draftToDocument(draft)
            this.telegramRequestService.send("Draft ${draft.docNum} passou pelo processo de autorizacao com sucesso!")
        }
    }

    fun aprovaEhCria(it: ApprovalRequests) {
        val draft = draftsService.getById(it.draftEntry!!).tryGetValue<OrderSales>()
        if(!draft.isCalculaDesonaerado())
            aprovaEhCria(draft,it)
    }
}