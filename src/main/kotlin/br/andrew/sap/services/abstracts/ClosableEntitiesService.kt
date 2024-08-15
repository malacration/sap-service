package br.andrew.sap.services.abstracts

import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.SapError
import br.andrew.sap.model.sap.Session
import br.andrew.sap.services.AuthService
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URLDecoder

interface ClosableEntitiesService<T> : EntitiesBase {


    fun close(id : String) {
        try{
            val request = RequestEntity
                .post(getHost()+path()+"(${id})/Close")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
            getRestTemplate().exchange(request, OData::class.java)
        }catch (t : HttpClientErrorException){
            throw t.getResponseBodyAs(SapError::class.java)?.getError(t,id) ?: t
        }
    }
}





