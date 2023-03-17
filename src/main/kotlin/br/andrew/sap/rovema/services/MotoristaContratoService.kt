package br.andrew.sap.rovema.services

import br.andrew.sap.rovema.infrastructure.odata.Condicao
import br.andrew.sap.rovema.infrastructure.odata.Filter
import br.andrew.sap.rovema.infrastructure.odata.OData
import br.andrew.sap.rovema.infrastructure.odata.Predicate
import br.andrew.sap.rovema.model.MotoristaContrato
import br.andrew.sap.rovema.model.MotoristaPecuaria
import br.andrew.sap.rovema.model.SapEnvrioment
import br.andrew.sap.rovema.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class MotoristaContratoService(env : SapEnvrioment,
                               restTemplate: RestTemplate,
                               authService: AuthService)
    : EntitiesService<MotoristaContrato>(env, restTemplate,authService) {
    override fun path(): String {
        return "/b1s/v1/AMFS_UDO_MTRT"
    }
}