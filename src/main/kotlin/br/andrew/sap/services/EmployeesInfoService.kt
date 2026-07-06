package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.EmployeesInfo
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserSourceService
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.document.DownPaymentService
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class EmployeesInfoService(restTemplate: RestTemplate,
                           val filiaisService : BussinessPlaceService,
                           env: SapEnvrioment,
                           authService : AuthService)
    : EntitiesService<EmployeesInfo>(env,restTemplate, authService), UserSourceService  {

    val logger = LoggerFactory.getLogger(EmployeesInfoService::class.java)

    override fun path(): String {
        return "/b1s/v1/EmployeesInfo"
    }

    /**
     * Retorna o codigo do vendedor vinculado ao colaborador (pelo EmployeeID),
     * ou `null` quando nao ha vinculo. Resultado cacheado para nao consultar
     * o SAP a cada requisicao (usado na autenticacao via Keycloak).
     */
    @Cacheable("employee-salesperson")
    fun getSalesPersonCodeByEmployeeId(employeeId: Int): Int? {
        return try {
            val filter = Filter(
                Predicate("EmployeeID", employeeId, Condicao.EQUAL),
                Predicate("Active", YesNo.tYES, Condicao.EQUAL))
            get(filter).tryGetValues<EmployeesInfo>()
                .firstOrNull()
                ?.SalesPersonCode
                ?.takeIf { it > 0 }
        } catch (e: Exception) {
            logger.warn("Falha ao buscar vendedor vinculado ao colaborador $employeeId", e)
            null
        }
    }

    override fun getByUserName(username: String): User {
        val filter = Filter(
            Predicate("eMail", username, Condicao.EQUAL),
            Predicate("Active", YesNo.tYES, Condicao.EQUAL))
        val resultado = get(filter).tryGetValues<EmployeesInfo>()
        if(resultado.size > 1)
            throw Exception("Existe mais de um usuario com o mesmo email")
        val colaborador = resultado.firstOrNull()?.also {
            it._bussinesPlace = filiaisService.getFilialByEmployee(it.EmployeeID).map { it.BPLId }
        } ?: throw Exception("Usuario nao encontrado")
        return colaborador.getUser()
    }

    override fun changePassword(user : User, hashedPassword : String) : OData?{
        val entry = "{\"U_password\": \"$hashedPassword\"}"
        return update(entry,user.id)
    }
}