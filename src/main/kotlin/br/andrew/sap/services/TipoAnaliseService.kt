package br.andrew.sap.services

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.romaneio.TipoAnalise
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class TipoAnaliseService(restTemplate: RestTemplate,
                         env: SapEnvrioment,
                         authService : AuthService) : EntitiesService<TipoAnalise>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/PECU_UDO_REGR"
    }
}