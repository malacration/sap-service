package br.andrew.sap.services

import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.services.abstracts.EntitiesService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class DummyService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<DocumentWrapper>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/OrdersService_GetApprovalTemplates"
    }
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class DocumentWrapper(val Document : OrderSales

)