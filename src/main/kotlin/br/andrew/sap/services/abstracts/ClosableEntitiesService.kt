package br.andrew.sap.services.abstracts

import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.sap.comercial.DocEntry
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.sistema.SapError
import br.andrew.sap.model.sap.sistema.Session
import br.andrew.sap.services.security.AuthService
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
                .header("cookie", session().cookieHeader())
                .build()
            getRestTemplate().exchange(request, OData::class.java)
        }catch (t : HttpClientErrorException){
            throw t.getResponseBodyAs(SapError::class.java)?.getError(t,id) ?: t
        }
    }
}





