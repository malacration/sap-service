package br.andrew.sap.services.bank

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class VendorPaymentService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<Payment>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/VendorPayments"
    }


    fun getPaymentBy(docEntry : String) : OData {
        val url = env.host+"/b1s/v1/SQLQueries"
        val pagamentos = restTemplate.exchange(
            RequestEntity
            .get("$url('pagamento-by-docEntry.sql')/List?DocEntry=$docEntry")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build(), OData::class.java).body!!.tryGetValues<DocEntry>()
        return this.get(Filter(
            Predicate("DocEntry",pagamentos.map { it.DocEntry },Condicao.IN),
            Predicate(Cancelled.tNO,Condicao.EQUAL),
        ))
    }


}