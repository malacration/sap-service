package br.andrew.sap.infrastructure.create.views

import br.andrew.sap.services.structs.QuerysServices
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["sql.expose-view"], havingValue = "true", matchIfMissing = false)
class ExposeView(val queryService: QuerysServices) {

    val logger = LoggerFactory.getLogger(ExposeView::class.java)

    @Bean("expose-view")
    fun exposeView(): String {
        listOf("ClientesVendedorB1SLQuery").forEach {
            //queryService.expose(it)
        }
        return "true"
    }
}