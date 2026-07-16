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

    /**
     * Tipos de objeto (SrcObjTyp) das contrapartidas com que [document] está conciliado em
     * reconciliações internas não canceladas. 13 = nota de saída, 14 = devolução,
     * 30 = lançamento contábil (reclassificação).
     */
    fun contrapartidasReconciliacao(document : Document): List<Int> {
        return contrapartidasReconciliacao(document.docEntry ?: -1, document.docObjectCode?.value ?: 13)
    }

    /**
     * ReconNum das reconciliações internas ativas que ligam especificamente os documentos A e B
     * (cada um por SrcObjTyp + SrcObjAbs). Permite cancelar só a conciliação de interesse, sem
     * afetar outras conciliações do documento.
     */
    fun reconciliacaoEntre(docEntryA : Int, objTypeA : Int, docEntryB : Int, objTypeB : Int): List<RecomNum> {
        return sqlQueriesService.execute(
            "reconciliacao-interna-entre.sql",
            listOf(
                Parameter("objTypeA", objTypeA),
                Parameter("docEntryA", docEntryA),
                Parameter("objTypeB", objTypeB),
                Parameter("docEntryB", docEntryB))
        )?.tryGetValues<RecomNum>() ?: listOf()
    }

    fun contrapartidasReconciliacao(docEntry : Int, objType : Int = 13): List<Int> {
        return sqlQueriesService.execute(
            "reconciliacao-interna-contrapartida.sql",
            listOf(
                Parameter("objType",objType),
                Parameter("docEntry",docEntry))
        )?.tryGetValues<ContrapartidaReconciliacao>()?.map { it.SrcObjTyp } ?: listOf()
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class RecomNum(val ReconNum : Int)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ContrapartidaReconciliacao(val SrcObjTyp : Int)