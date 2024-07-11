package br.andrew.sap.services.structs

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.romaneio.RomaneioPesagem
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.schedules.CreateSignatureTaskSe
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UserObjectsMDService(restTemplate: RestTemplate,
                           env: SapEnvrioment,
                           authService : AuthService
) : EntitiesService<UserDefinedObject>(env,restTemplate, authService) {

    val logger: Logger = LoggerFactory.getLogger(UserObjectsMDService::class.java)

    override fun path(): String {
        return "/b1s/v1/UserObjectsMD"
    }

    fun findOrCreate(ud : UserDefinedObject) {
        logger.info("Verificando se UDO ${ud.Name} existe")
        val predicates = mutableListOf(
            Predicate("Code",ud.Code, Condicao.EQUAL),
        )
        val result = get(Filter(predicates))
        if(result.tryGetValues<UserDefinedObject>().isEmpty()){
            save(ud)
        }
    }

}