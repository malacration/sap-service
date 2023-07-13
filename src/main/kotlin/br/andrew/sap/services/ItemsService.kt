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
                   @Qualifier("base.price.list.id") val priceListId : String,
        )
    : EntitiesService<Item>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/Items"
    }

    fun getPriceBase(itemCode: String): Double {
        if(priceListId == "none")
            return 0.0
        return getById("'$itemCode'")
            .tryGetValue<Item>().itemPrices.first { it.PriceList == priceListId }?.Price ?: 0.0

    }

    fun getPriceBase(documentLine: Product): Double {
        return getPriceBase(documentLine.ItemCode!!)
    }
}


