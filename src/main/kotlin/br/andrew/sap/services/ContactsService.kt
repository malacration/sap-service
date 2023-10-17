package br.andrew.sap.services

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.partner.Person
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ContactsService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<Person>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/Contacts"
    }
}