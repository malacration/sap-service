package br.andrew.sap.services.stock

import br.andrew.sap.model.estoque.Item
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
open class ResourceService(
    env : SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService,
    val sqlQueriesService: SqlQueriesService,
) : EntitiesService<Item>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/Resources"
    }
}


