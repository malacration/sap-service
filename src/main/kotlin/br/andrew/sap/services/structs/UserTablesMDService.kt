package br.andrew.sap.services.structs

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.TableMd
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UserTablesMDService(env: SapEnvrioment, restTemplate: RestTemplate,
                          authService: AuthService) : EntitiesService<TableMd>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/UserTablesMD"
    }

    val logger = LoggerFactory.getLogger(UserTablesMDService::class.java)

    fun findOrCreate(table : TableMd){
        logger.info("Verificando se a tabela ${table.tableName} existe")
        val predicates = mutableListOf(
            Predicate("TableName",table.tableName, Condicao.EQUAL),
        )
        val result = get(Filter(predicates))
        if(result.tryGetValues<TableMd>().isEmpty()){
            save(table)
        }
    }


}