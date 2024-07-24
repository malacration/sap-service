package br.andrew.sap.model.exceptions

import br.andrew.sap.infrastructure.configurations.EventPublisherSingleton
import br.andrew.sap.model.sap.SapError
import br.andrew.sap.model.sap.documents.base.Document

class BusinessPartnerNotAssignedException(error: SapError, val entry : Document) : SapGenericException(error, error.throwable) {
    init {
        EventPublisherSingleton.instance.publishEvent(this)
    }
}