package br.andrew.sap.services

import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Localidade
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class LocalidadeService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
    EntitiesService<Localidade>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/Locais"
    }
}