package br.andrew.sap.services.calculadora

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.calculadora.CalculadoraPreco
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Branch
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CalculadoraService(
    val sqlQuerysServices : SqlQueriesService,
    env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService
) :
        EntitiesService<CalculadoraPreco>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/AR_CALC_PRECO"
    }
}