package br.andrew.sap.services

import br.andrew.sap.model.MotoristaContrato
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
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