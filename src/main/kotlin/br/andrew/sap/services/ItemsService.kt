package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.Item
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.documents.base.Product
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class ItemsService(env : SapEnvrioment,
                   restTemplate: RestTemplate,
                   authService: AuthService,
                   val sqlQueriesService: SqlQueriesService
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

    fun fullSearchText(keyWord : String, priceListId : Int): Any {
        val parameters = listOf(
            Parameter("search","'%$keyWord%'"),
            Parameter("zero",0),
            Parameter("vendedor",27)
        )
        return sqlQueriesService.execute("produto-tabela.sql", parameters)!!.tryGetNextValues<String>()
    }


}


