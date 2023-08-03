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
class ApprovalStagesService(env : SapEnvrioment,
                            restTemplate: RestTemplate,
                            authService: AuthService,
                            val draftsService: DraftsService
) : EntitiesService<String>(env, restTemplate,authService) {
    override fun path(): String {
        return "/b1s/v1/ApprovalStages"
    }
}