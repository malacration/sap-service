package br.andrew.sap.services.bank

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.payment.PaymentTermsTypes
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.payment.PaymentTermsLines
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class PaymentTermsTypesService(
    val sqlQueriesService : SqlQueriesService,
    env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<PaymentTermsTypes>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/PaymentTermsTypes"
    }


    fun getParcelas(id : Int): List<PaymentTermsLines> {
        return sqlQueriesService
            .execute("parcelas-pagamento.sql", Parameter("id",id))
            ?.tryGetValues<PaymentTermsLines>() ?: throw Exception("Condicao de pagamento nao encontrada")
    }
}