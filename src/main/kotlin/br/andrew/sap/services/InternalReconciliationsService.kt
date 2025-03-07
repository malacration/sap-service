package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.InternalReconciliations
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.*
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.util.*

@Service
class InternalReconciliationsService(
    val sqlQueriesService: SqlQueriesService,
    env: SapEnvrioment,
    restTemplate: RestTemplate,
     authService: AuthService) :
        EntitiesService<InternalReconciliations>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/InternalReconciliations"
    }

    override fun get(filter: Filter, order: OrderBy, page: Pageable): OData {
        throw Exception("Metodo nao suportado")
    }

    fun reconciliacaoByDocument(document : Document): List<RecomNum> {
        return reconciliacaoByDocument(document.docObjectCode?.value ?: 13,document.docEntry ?: -1)
    }

    fun reconciliacaoByDocument(docEntry : Int,objType : Int): List<RecomNum> {
        return sqlQueriesService.execute(
            "reconciliacao-interna-document.sql",
            listOf(
                Parameter("objType",objType),
                Parameter("docEntry",docEntry))
        )?.tryGetValues<RecomNum>() ?: listOf()
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class RecomNum(val ReconNum : Int)