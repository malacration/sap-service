package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.ParcelasAberto
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.SapError
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.partner.CpfCnpj
import br.andrew.sap.services.abstracts.EntitiesService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


@Service
class SerasaService(@Value("\${serasa.url:http://localhost:8081}") val serasaUrl : String,
                    env : SapEnvrioment,
                    restTemplate: RestTemplate,
                    authService: AuthService) : EntitiesService<Document>(env, restTemplate,authService) {

    override fun path(): String {
        return ""
    }

    fun getParceirosParaAtualizar() : List<String> {
        val cal: Calendar = GregorianCalendar()
        cal.time = Date()
        cal.add(Calendar.DAY_OF_MONTH, -10)
        val date = SimpleDateFormat("yyyy-MM-dd").format(cal.time)
        val url = env.host+"/b1s/v1/"
        var uri = "${url}SQLQueries('atualiza-serasa.sql')/List?data='$date'"

        val request = RequestEntity
            .get(uri)
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        val odata = restTemplate.exchange(request, OData::class.java).body
        return odata?.tryGetValues<Map<String,String>>()?.flatMap { it.values.toList() } ?: listOf()
    }

    fun atualizaSerasa(cpfCnpj: CpfCnpj): ResponseEntity<String> {
        var uri = "${serasaUrl}/serasa/${cpfCnpj.value}"
        val request = RequestEntity
            .get(uri)
            .build()
        return restTemplate.exchange(request, String::class.java)
    }
}

