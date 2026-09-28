package br.andrew.sap.services.cadastro

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.sap.cadastro.ProfitCenter
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ProfitCenterService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<ProfitCenter>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/ProfitCenters"
    }

    fun inativos(): List<ProfitCenter> {
        return getAll(ProfitCenter::class.java, Filter(Predicate("Active", YesNo.tNO, Condicao.EQUAL)))
    }
}
