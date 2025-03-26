package br.andrew.sap.services

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.partner.OrdemCarregamento
import br.andrew.sap.model.sap.partner.Person
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OrdemCarregamentoService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<OrdemCarregamento>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/RO_ORD_CARREGAMENTO"
    }
}