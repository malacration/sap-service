package br.andrew.sap.infrastructure.create

import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.TransactionCodesService
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class TransactionCodesConfiguration(val service : TransactionCodesService) {

    init {
        TransactionCodeTypes.values().forEach {
            service.saveIsNotPreset(it)
        }
    }
}