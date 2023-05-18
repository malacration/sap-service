package br.andrew.sap.services.structs

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.FieldMd
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UserFieldsMDService(env: SapEnvrioment, restTemplate: RestTemplate,
                          authService: AuthService) : EntitiesService<FieldMd>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/UserFieldsMD"
    }

    fun findOrCreate(field: FieldMd) {
        val predicates = listOf(
                Predicate("Name",field.name,Condicao.EQUAL),
                Predicate("TableName",field.tableName,Condicao.EQUAL)
        )
com         val result = get(Filter(predicates))
        if(result.tryGetValues<FieldMd>().isEmpty()){
            save(field)
        }
    }
}