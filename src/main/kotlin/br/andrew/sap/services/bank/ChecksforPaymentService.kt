package br.andrew.sap.services.bank

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class ChecksforPaymentService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<Payment>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/ChecksforPayment"
    }
}