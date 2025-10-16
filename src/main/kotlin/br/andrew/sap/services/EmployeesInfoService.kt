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
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class EmployeesInfoService(restTemplate: RestTemplate,
                           env: SapEnvrioment,
                           authService : AuthService)
    : EntitiesService<EmployeesInfo>(env,restTemplate, authService), UserSourceService  {


    override fun path(): String {
        return "/b1s/v1/EmployeesInfo"
    }

    override fun getByUserName(username: String): User {
        val filter = Filter(
            Predicate("eMail", username, Condicao.EQUAL),
            Predicate("Active", YesNo.tYES, Condicao.EQUAL))
        val resultado = get(filter).tryGetValues<EmployeesInfo>()
        if(resultado.size > 1)
            throw Exception("Existe mais de um usuario com o mesmo email")
        return resultado.firstOrNull()?.getUser() ?: throw Exception("Usuario nao encontrado")
    }

    override fun changePassword(user : User, hashedPassword : String) : OData?{
        val entry = "{\"U_password\": \"$hashedPassword\"}"
        return update(entry,user.id)
    }
}