package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SalesPersonsService(val sqlQueriesService : SqlQueriesService , env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<SalePerson>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/SalesPersons"
    }

    fun getEnviaRelatorio(): List<SalePerson> {
        val filter = Filter("U_envia_relatorio","1",Condicao.EQUAL)
        return getAll(filter)
    }

    fun search(keyword: String, page : Pageable): Page<SalePerson>? {
        val filter = Filter(
            Predicate("SalesEmployeeName", keyword,Condicao.CONTAINS ),
            Predicate("SalesEmployeeCode",-1,Condicao.NOT_EQUAL)
        )
        val result = get(filter)
        return result.tryGetPageValues<SalePerson>(page)
    }

    fun isSalesPersonActive(salesPersonCode: Int): Boolean {
        val filter = Filter(
            Predicate("SalesEmployeeCode", salesPersonCode, Condicao.EQUAL),
            Predicate("Active", "Y", Condicao.EQUAL)
        )
        return get(filter).tryGetValues<SalePerson>().isNotEmpty()
    }

    fun getByUserName(username: String): SalePerson {
        val filter = if(username.toIntOrNull() != null)
            Filter(Predicate("SalesEmployeeCode", username.toInt(), Condicao.EQUAL),Predicate("Active", YesNo.tYES, Condicao.EQUAL))
        else{
            Filter(Predicate("Email", username, Condicao.EQUAL),Predicate("Active", YesNo.tYES, Condicao.EQUAL))
        }
        val resultado = get(filter).tryGetValues<SalePerson>().filter { it.Active == "tYES" }
        if(resultado.size > 1)
            throw Exception("Existe mais de um usuario com o mesmo email")
        return resultado.firstOrNull() ?: throw Exception("Usuario nao encontrado")
    }

}