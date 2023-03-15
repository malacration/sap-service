package br.andrew.sap.rovema.services

import br.andrew.sap.rovema.model.SapEnvrioment
import br.andrew.sap.rovema.model.romaneio.RomaneioEntradaInsumoMin
import br.andrew.sap.rovema.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RomaneioEntradaInsumoService(restTemplate: RestTemplate,
                                   env: SapEnvrioment,
                                   authService : AuthService) : EntitiesService<RomaneioEntradaInsumoMin>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/PECU_UDO_REGR"
    }
}