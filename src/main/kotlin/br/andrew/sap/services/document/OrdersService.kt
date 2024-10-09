package br.andrew.sap.services.document

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.ClosableEntitiesService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OrdersService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<Document>(env, restTemplate, authService), ClosableEntitiesService<OrderSales> {
    override fun path(): String {
        return "/b1s/v1/Orders"
    }
}