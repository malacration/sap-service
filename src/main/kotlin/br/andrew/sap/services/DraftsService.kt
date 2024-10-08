package br.andrew.sap.services

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.SapError
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.abstracts.EntitiesService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
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
            restT.exchange(request, String::class.java)
        }catch (t : HttpClientErrorException){
            throw t.getResponseBodyAs(SapError::class.java)?.getError(t,documento) ?: t
        }
    }

    @JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    class DraftWrapper(document : Document){

        val document : Document
        init {
            this.document = Document(document.CardCode,document.DocDueDate, listOf(),document.getBPL_IDAssignedToInvoice())
            this.document.docEntry = document.docEntry
            document.discountPercent = null;
            document.totalDiscount = null;
        }
    }
}

