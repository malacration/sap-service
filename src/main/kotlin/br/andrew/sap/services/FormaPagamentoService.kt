package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.PrazoPagamentoDto
import br.andrew.sap.model.payment.PaymentMethodDto
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service

@Service
class FormaPagamentoService(val sqlQueriesService : SqlQueriesService,) {


    fun getByFilial(idBranch : Int, cardCode : String): List<PaymentMethodDto>? {
        return sqlQueriesService
            .execute("forma-pagamento.sql",
                listOf(Parameter("idBranch",idBranch),
                    Parameter("cardCode",cardCode))
            )
            ?.tryGetValues<PaymentMethodDto>()
    }
}