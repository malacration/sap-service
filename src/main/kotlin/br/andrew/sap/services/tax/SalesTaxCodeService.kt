package br.andrew.sap.services.tax

import br.andrew.sap.model.sap.tax.SalesTaxCode
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SalesTaxCodeService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<SalesTaxCode>(env, restTemplate, authService) {


    override fun path(): String {
        return "/b1s/v1/SalesTaxCodes"
    }
}