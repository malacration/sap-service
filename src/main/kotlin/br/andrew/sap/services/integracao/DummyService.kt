package br.andrew.sap.services.integracao
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.services.abstracts.EntitiesService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService

@Service
class DummyService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<DocumentWrapper>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/OrdersService_GetApprovalTemplates"
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class DocumentWrapper(val Document : OrderSales

)