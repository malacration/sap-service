package br.andrew.sap.services

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ProductionOrdersService(
    env: SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService
) : EntitiesService<Map<String, Any>>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/ProductionOrders"
    }
}
