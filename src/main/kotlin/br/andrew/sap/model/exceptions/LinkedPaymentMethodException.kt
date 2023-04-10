package br.andrew.sap.model.exceptions

import br.andrew.sap.model.SapError

class LinkedPaymentMethodException(error: SapError, cause: Throwable? = null) : SapGenericException(error, cause) {
}