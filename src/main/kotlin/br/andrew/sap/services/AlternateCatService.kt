package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.Item
import br.andrew.sap.model.ItemAlternate
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.documents.Product
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class AlternateCatService(env : SapEnvrioment,
                          restTemplate: RestTemplate,
                          authService: AuthService)
    : EntitiesService<Item>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/AlternateCatNum"
    }

    fun get(itemCode: String, cardCode: String): ItemAlternate? {
        val filter = Filter(
            Predicate("ItemCode", itemCode,Condicao.EQUAL ),
            Predicate("CardCode",itemCode,Condicao.EQUAL))
        return get(filter).tryGetValues<ItemAlternate>().firstOrNull()
    }
}


