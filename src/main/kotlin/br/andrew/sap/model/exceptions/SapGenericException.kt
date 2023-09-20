package br.andrew.sap.model.exceptions

import br.andrew.sap.infrastructure.configurations.EventPublisherSingleton
import br.andrew.sap.model.SapError
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.base.Document

open class SapGenericException(val erro : SapError, val causa : Throwable? = null, extra : String = "") :
        Exception(erro.error.message.value+" - "+extra,causa)

class CreditException(error: SapError, val location : String?, cause: Throwable? = null) : SapGenericException(error, cause, "Detecção de erro relacionado a Credito do PN") {
    //TODO é possivel fazer a chamada a draft para verificar os valores

    val idLocation : String = "(\\d+)(?=\\)$)".toRegex().find(location?:"")?.value ?: ""

    fun getDocumentFake(order: Document? = null) : Document{
        return order?.also {
            it.docEntry = idLocation.toInt()
            it.docNum = idLocation
        }
            ?: OrderSales("CardCode", "", listOf(),"")
                .also {
                    it.docEntry = idLocation.toInt()
                    it.docNum = idLocation
                }
    }
}
class LinkedPaymentMethodException(error: SapError) : SapGenericException(error, error.throwable) {
    init {
        EventPublisherSingleton.instance.publishEvent(this)
    }
}