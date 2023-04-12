package br.andrew.sap.services

import br.andrew.sap.controllers.DocumentWrapper
import br.andrew.sap.model.BusinessPartner
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class DummyService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<DocumentWrapper>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/OrdersService_GetApprovalTemplates"
    }
}