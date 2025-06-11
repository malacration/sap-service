package br.andrew.sap.services

import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Carregamento
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CarregamentoService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<Carregamento>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/ORD_CARREGAMENTO"
    }
}