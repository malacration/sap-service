package br.andrew.sap.model

import br.andrew.sap.model.exceptions.LinkedPaymentMethodException
import br.andrew.sap.model.exceptions.SapGenericException


class SapError(val error : Error) {
    fun getError() : Throwable {
        return error.getError(this) ?: SapGenericException(this)
    }
}

class Error(val code : String, val message : SapMessage) {
    fun getError(error : SapError) : Throwable? {
        return message.getError(error)
    }
}

class SapMessage(val lang : String, val value : String) {
    fun getError(error: SapError): Throwable? {
        return if(value.contains("Linked payment method"))
            LinkedPaymentMethodException(error)
        else null
    }
}