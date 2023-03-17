package br.andrew.sap.rovema.services

import br.andrew.sap.rovema.infrastructure.odata.Condicao
import br.andrew.sap.rovema.infrastructure.odata.Filter
import br.andrew.sap.rovema.infrastructure.odata.OData
import br.andrew.sap.rovema.infrastructure.odata.Predicate
import br.andrew.sap.rovema.model.MotoristaPecuaria
import br.andrew.sap.rovema.model.SapEnvrioment
import br.andrew.sap.rovema.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MotoristaPecuariaService(env : SapEnvrioment,
                               restTemplate: RestTemplate,
                               authService: AuthService)
    : EntitiesService<MotoristaPecuaria>(env, restTemplate,authService) {
    override fun path(): String {
        return "/b1s/v1/PECU_UDO_MTRT"
    }

    fun getByCnh(cnh : String): OData {
        return get(Filter(
                listOf(
                        Predicate("U_RegistroCNH",cnh, Condicao.EQUAL)
                )));
    }
}