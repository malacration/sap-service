package br.andrew.sap.rovema.services

import br.andrew.sap.rovema.model.RomaneioPesagem
import br.andrew.sap.rovema.model.SapEnvrioment
import br.andrew.sap.rovema.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RomaneioPesagemService(restTemplate: RestTemplate,
                             env: SapEnvrioment,
                             authService : AuthService) : EntitiesService<RomaneioPesagem>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/AMFS_UDO_RETR"
    }

}