package br.andrew.sap.services

import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.SapError
import br.andrew.sap.model.documents.Document
import br.andrew.sap.services.abstracts.EntitiesService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate


@Service
class DraftsService(env : SapEnvrioment,
                    restTemplate: RestTemplate,
                    authService: AuthService) : EntitiesService<Document>(env, restTemplate,authService) {
    override fun path(): String {
        return "/b1s/v1/Drafts"
    }

    /**
     * POST https://localhost:50000/b1s/v1/DraftsService_SaveDraftToDocument
     *
     * {
     *     "Document": {
     *         "DocDueDate": "2017-01-28",
     *         "DocEntry": "3"
     *     }
     * }
     */
    fun draftToDocument(documento : Document) {
        try{
            val url = env.host+this.path()+"Service_SaveDraftToDocument"
            val request = RequestEntity
                    .post(url)
                    .header("cookie","B1SESSION=${session().sessionId}")
                    .body(DraftWrapper(documento))
            restTemplate.exchange(request, String::class.java)
        }catch (t : HttpClientErrorException){
            throw t.getResponseBodyAs(SapError::class.java)?.getError(t,documento) ?: t
        }
    }

    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class DraftWrapper(val document : Document){

    }
}

