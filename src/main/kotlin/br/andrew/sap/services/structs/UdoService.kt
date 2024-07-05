package br.andrew.sap.services.structs

import br.andrew.sap.controllers.documents.InvoicesController
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.TableMd
import br.andrew.sap.model.entity.UserDefinedObject
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UdoService(env: SapEnvrioment, restTemplate: RestTemplate,
                 authService: AuthService) : EntitiesService<UserDefinedObject>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/UserObjectsMD"
    }

//    val logger = LoggerFactory.getLogger(UdoService::class.java)
//
    fun findOrCreate(userDefi : UserDefinedObject){
//        val predicates = mutableListOf(
//            Predicate("TableName",table.tableName, Condicao.EQUAL),
//        )
//        val result = get(Filter(predicates))
//        if(result.tryGetValues<TableMd>().isEmpty()){
//            save(table)
//        }
    }


}