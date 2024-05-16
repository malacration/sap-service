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
import java.io.File


@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["sql"], havingValue = "true", matchIfMissing = true)
class ProvisioningConfiguration(val queryService: QuerysServices) {

    val logger = LoggerFactory.getLogger(ProvisioningConfiguration::class.java)

    @Bean
    fun createQuerys(): String {
        ClassPathResource("views").file.walk()
            .filter { it.isFile }
            .map {
                Query(it.name,it.name,it.readText())
            }.forEach {
                logger.info("Atualizando view {${it.sqlName}}")
                queryService.replace(it)
            }
        return "true"
    }
}