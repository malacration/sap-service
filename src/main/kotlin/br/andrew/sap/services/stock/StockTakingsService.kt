package br.andrew.sap.services.stock

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.estoque.Item
import br.andrew.sap.model.calculadora.Produto
import br.andrew.sap.model.calculadora.ProdutoSelecao
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.RequestEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit


@Service
open class StockTakingsService(
    env : SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService,
    val sqlQueriesService: SqlQueriesService,
    val cacheManager : CacheManager
) : EntitiesService<Item>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/StockTakings"
    }


    fun delete(itemCode : String, wareHouseCode : String) : OData? {
        val parametro = "ItemCode='${itemCode}',WarehouseCode='$wareHouseCode'"
        val request = RequestEntity
            .delete(env.host+this.path()+"(${parametro})")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restT.exchange(request, OData::class.java).body
    }
}


