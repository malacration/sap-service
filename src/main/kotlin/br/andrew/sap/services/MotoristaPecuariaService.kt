package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.MotoristaPecuaria
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
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
                        Predicate("U_RegistroCNH",cnh.trim().trimStart('0'), Condicao.CONTAINS)
                )))
    }
}