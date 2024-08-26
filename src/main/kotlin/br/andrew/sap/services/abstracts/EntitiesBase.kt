package br.andrew.sap.services.abstracts

import br.andrew.sap.model.sap.Session
import org.springframework.web.client.RestTemplate


public interface EntitiesBase {

    fun getHost() : String

    fun path() : String

    fun session() : Session

    fun getRestTemplate() : RestTemplate
}
