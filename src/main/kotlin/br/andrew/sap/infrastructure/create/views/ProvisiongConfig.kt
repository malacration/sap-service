package br.andrew.sap.infrastructure.create.views

import br.andrew.sap.model.Query
import br.andrew.sap.services.structs.QuerysServices
import br.andrew.sap.services.structs.UserFieldsMDService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["sql"], havingValue = "true", matchIfMissing = true)
class ProvisioningConfiguration(val queryService: QuerysServices) {

    val logger = LoggerFactory.getLogger(ProvisioningConfiguration::class.java)

    @Bean
    fun createQuerys(): String {
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources("classpath:/views/**/*.sql") // Ajuste o caminho conforme necessário

        resources.forEach { resource ->
            val inputStream = resource.inputStream
            val content = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            if(resource.filename != null)
                queryService.replace(Query(resource.filename!!, resource.filename!!, content))
            logger.info("Atualizando view {${resource.filename}}")
        }
        return "true"
    }
}