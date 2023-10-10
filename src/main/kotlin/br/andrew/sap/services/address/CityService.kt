package br.andrew.sap.services.address

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.ParcelasAberto
import br.andrew.sap.model.Session
import br.andrew.sap.model.address.City
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.romaneio.TipoAnalise
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CityService(val env : SapEnvrioment,
                  val restTemplate: RestTemplate,
                  val authService: AuthService)  {

    fun session() : Session {
        return authService.getToken(env.getLogin())
    }

    @Cacheable("citys")
    fun get(estado : String) : List<City> {
        val url = env.host+"/b1s/v1/"
        var uri = "${url}SQLQueries('cidades.sql')/List?\$skip=${0}&estado='${estado}'"
        val resultado : MutableList<City> = mutableListOf()
        do{
            val request = RequestEntity
                .get(uri)
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
            val odata = restTemplate.exchange(request, OData::class.java).body
            uri = (url + odata?.nextLink())
            resultado.addAll(odata!!.tryGetValues())
        } while (odata?.hasNext() ?: false)

        return resultado
    }

}