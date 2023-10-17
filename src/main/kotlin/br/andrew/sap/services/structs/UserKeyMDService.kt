package br.andrew.sap.services.structs

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.entity.UserKeyMD
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UserKeyMDService(env: SapEnvrioment, restTemplate: RestTemplate,
                       authService: AuthService) : EntitiesService<UserKeyMD>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/UserKeysMD"
    }

    val logger = LoggerFactory.getLogger(UserKeyMDService::class.java)

    fun findOrCreate(key: UserKeyMD) {

        logger.info("Verificando se a key ${key.KeyName} existe")

        val predicates = listOf(
                Predicate("KeyName",key.KeyName,Condicao.EQUAL),
                Predicate("TableName",key.TableName,Condicao.EQUAL)
        )
        val result = get(Filter(predicates))
        if(result.tryGetValues<UserKeyMD>().isEmpty()){
            save(key)
        }
    }
}

