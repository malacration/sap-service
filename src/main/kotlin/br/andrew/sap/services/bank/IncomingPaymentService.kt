package br.andrew.sap.services.bank

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.DocEntry
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class IncomingPaymentService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<Payment>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/IncomingPayments"
    }


    fun installPayment(docEntry : Int) : Any{
        val url = env.host+"/b1s/v1/SQLQueries"
        return restTemplate.exchange(
            RequestEntity
                .get("$url('parcelas-pagas.sql')/List?docEntryInvoice=$docEntry")
                .header("cookie","B1SESSION=${session().sessionId}")
                .build(), OData::class.java).body!!.tryGetValues<Teste>()
    }

}

class Teste(val DocNum : String, val InstId : String)