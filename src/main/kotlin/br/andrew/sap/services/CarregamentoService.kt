package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Carregamento
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CarregamentoService(val sqlQueriesService : SqlQueriesService,env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<Carregamento>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/ORD_CARREGAMENTO"
    }

    fun getEstoqueEmCarregamento(ItemCode: String): Int {
        val parameters = listOf(
            Parameter("ItemCode", ItemCode)
        )
        return sqlQueriesService
            .execute("estoque-em-ordem.sql", parameters)
            ?.tryGetValues<Map<String, Any>>()
            ?.firstOrNull()
            ?.get("total_quantidade") as Int? ?: 0
    }
}