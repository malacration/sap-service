package br.andrew.sap.services.stock

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.Item
import br.andrew.sap.model.calculadora.Produto
import br.andrew.sap.model.calculadora.ProdutoSelecao
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


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


