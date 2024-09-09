package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.PrazoPagamentoDto
import br.andrew.sap.model.payment.PaymentMethodDto
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service

@Service
class FormaPagamentoService(val sqlQueriesService : SqlQueriesService,) {


    fun getByFilial(idBranch : Int): List<PaymentMethodDto>? {
        return sqlQueriesService
            .execute("forma-pagamento.sql", Parameter("idBranch",idBranch))
            ?.tryGetValues<PaymentMethodDto>()
    }
}