package br.andrew.sap.services

import br.andrew.sap.model.envrioments.SapEnvrioment
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BankPlusService(env: SapEnvrioment, val bpService: BusinessPartnersService,
                      restTemplate: RestTemplate, authService: AuthService) {

    fun path(): String {
        return "/b1s/v1/Attachments2"
    }

}