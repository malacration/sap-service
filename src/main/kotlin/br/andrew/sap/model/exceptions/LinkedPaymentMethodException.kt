package br.andrew.sap.model.exceptions

import br.andrew.sap.infrastructure.configurations.EventPublisherSingleton
import br.andrew.sap.model.SapError

class LinkedPaymentMethodException(error: SapError) : SapGenericException(error, error.throwable) {
    init {
        EventPublisherSingleton.instance.publishEvent(this)
    }
}