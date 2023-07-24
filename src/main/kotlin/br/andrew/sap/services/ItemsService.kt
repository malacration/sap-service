package br.andrew.sap.services

import br.andrew.sap.model.Item
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.documents.Product
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ItemsService(env : SapEnvrioment,
                   restTemplate: RestTemplate,
                   authService: AuthService,
        )
    : EntitiesService<Item>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/Items"
    }

    fun getPriceBase(itemCode: Product, priceListId : Int): Double {
        return getPriceBase(itemCode.id, priceListId)
    }

    fun getPriceBase(itemCode: String, priceListId : Int): Double {
        return getById("'$itemCode'")
            .tryGetValue<Item>()
            .itemPrices.filter { it.PriceList == priceListId }
            .firstOrNull()?.Price ?: throw Exception("Price[$priceListId] not found; ItemCode[$itemCode]")
    }
}


