package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.ParcelasAberto
import br.andrew.sap.model.Session
import br.andrew.sap.model.envrioments.SapEnvrioment
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ParcelasAbertoService(val env : SapEnvrioment,
                            val restTemplate: RestTemplate,
                            val authService: AuthService,
        ) {

    fun session() : Session {
        return authService.getToken(env.getLogin())
    }

    fun getAllBySql(slpCode : Int) : List<ParcelasAberto> {
        val url = env.host+"/b1s/v1/"
        var uri = "${url}SQLQueries('titulos.sql')/List?\$skip=${0}&slpCode=${slpCode}"
        val resultado : MutableList<ParcelasAberto> = mutableListOf()
        do{
            val request = RequestEntity
                .get(uri)
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
            val odata = restTemplate.exchange(request, OData::class.java).body
            uri = (url + odata?.nextLink())
            resultado.addAll(odata?.tryGetValues() ?: listOf())
        } while (odata?.hasNext() ?: false)

        return resultado.sortedBy { "${it.CardCode}-${it.Serial}-${it.InstlmntID}" }
    }
}


