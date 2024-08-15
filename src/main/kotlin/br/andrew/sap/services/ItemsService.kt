package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.Item
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.base.Product
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

    fun fullSearchText(keyWord : String, idVendedor : Int): NextLink<Product> {
        val parameters = listOf(
            Parameter("search","'%${keyWord.uppercase()}%'"),
            Parameter("zero",0),
            Parameter("yes","'Y'"),
            Parameter("vendedor",idVendedor)
        )
        if(keyWord.contains("SQLQueries('produto-tabela.sql')"))
            return sqlQueriesService.nextLink(keyWord)!!.tryGetNextValues<Product>()
        return sqlQueriesService.execute("produto-tabela.sql", parameters)!!.tryGetNextValues<Product>()
    }


}


