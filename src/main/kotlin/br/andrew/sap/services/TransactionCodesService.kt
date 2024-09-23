package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.transaction.TransactionCode
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class TransactionCodesService(restTemplate: RestTemplate,
                              env: SapEnvrioment,
                              authService : AuthService) : EntitiesService<TransactionCode>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/TransactionCodes"
    }

    fun saveIsNotPreset(type: TransactionCodeTypes) {
        val transactionCode = type.get()
        if(get(Filter("Code",transactionCode.Code,Condicao.EQUAL)).tryGetValues<TransactionCode>().isEmpty())
            save(transactionCode)
    }
}