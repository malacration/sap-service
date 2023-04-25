package br.andrew.sap.model

import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.exceptions.LinkedPaymentMethodException
import br.andrew.sap.model.exceptions.SapGenericException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.HttpClientErrorException


class SapError(val error : ErrorMsg) {

    var hedears : HttpHeaders? = null
    var httpStatusCode : HttpStatusCode? = null
    fun getError() : Throwable {
        return error.getError(this) ?: SapGenericException(this)
    }

    fun getError(t: HttpClientErrorException): Throwable? {
        this.hedears = t.responseHeaders
        this.httpStatusCode = t.statusCode
        return getError()
    }
}

class ErrorMsg(val code : String, val message : SapMessage) {
    fun getError(error : SapError) : Throwable? {
        return message.getError(error)
    }
}

class SapMessage(val lang : String, val value : String) {
    fun getError(error: SapError): Throwable? {
        return if(value.contains("Linked payment method"))
            LinkedPaymentMethodException(error)
        else if(error.error.code == "-2028" && error.hedears?.get("Location")?.get(0)?.contains("/b1s/v1/Drafts") ?: false)
            CreditException(error,error.hedears?.get("Location")?.get(0))
        else null
    }
}