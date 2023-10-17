package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.PaymentMethod
import br.andrew.sap.model.SalePerson
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.partner.BPBranchAssignment
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SalesPersonsService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<SalePerson>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/SalesPersons"
    }

    fun getEnviaRelatorio(): List<SalePerson> {
        val filter = Filter("U_envia_relatorio","1",Condicao.EQUAL)
        return get(filter).tryGetValues()
    }
}