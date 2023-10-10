package br.andrew.sap.services

import br.andrew.sap.infrastructure.configurations.SerasaProperties
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SerasaService(val conf: SerasaProperties,
                    val restTemplate: RestTemplate) {

    fun test() : Any? {
        val url = conf.getBase()
        val request = RequestEntity
            .post(url)
            .build()
        return restTemplate.exchange(request, String::class.java).body
    }
}