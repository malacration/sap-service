package br.andrew.sap.services.structs

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UserFieldsMDService(env: SapEnvrioment, restTemplate: RestTemplate,
                          authService: AuthService) : EntitiesService<FieldMd>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/UserFieldsMD"
    }

    val logger = LoggerFactory.getLogger(UserFieldsMDService::class.java)

    fun findOrCreate(field: FieldMd): OData {
        logger.info("Verificando se o campo ${field.name} existe")
        val predicates = mutableListOf(
                Predicate("Name",field.name,Condicao.EQUAL),
                Predicate("TableName",field.tableName,Condicao.EQUAL)
        )
        val result = get(Filter(predicates))
        return if(result.tryGetValues<FieldMd>().isEmpty())
            save(field)
        else
            result
    }

    fun replace(field: FieldMd): OData {
        logger.info("Verificando se o campo ${field.name} existe")
        val predicates = mutableListOf(
            Predicate("Name",field.name,Condicao.EQUAL),
            Predicate("TableName",field.tableName,Condicao.EQUAL)
        )
        val result = get(Filter(predicates))
        if(result.tryGetValues<FieldMd>().isNotEmpty()){
            result.tryGetValues<FieldMd>().forEach {
                logger.info("Deletando o campo ${field.name}")
                delete("TableName='${field.tableName}', FieldID=${it.FieldID}")
            }
        }
        logger.info("Recriando o campo ${field.name}")
        return save(field)
    }
}