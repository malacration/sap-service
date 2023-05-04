package br.andrew.sap.model.exceptions

import br.andrew.sap.model.SapError
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.sovis.PedidoVenda

open class SapGenericException(val erro : SapError, val causa : Throwable? = null, extra : String = "") :
        Exception(erro.error.message.value+" - "+extra,causa){

}

class CreditException(error: SapError, val location : String?, cause: Throwable? = null) : SapGenericException(error, cause, "Detecção de erro relacionado a Credito do PN") {
    //TODO é possivel fazer a chamada a draft para verificar os valores

    val idLocation : String = "(\\d+)(?=\\)$)".toRegex().find(location?:"")?.value ?: ""

    fun getOrderFake(pedido : PedidoVenda) : OrderSales{
        return pedido.getOrder().also {
            it.docNum = idLocation
            it.docEntry = idLocation.toIntOrNull()
        }

    }
}
class LinkedPaymentMethodException(error: SapError, cause: Throwable? = null) : SapGenericException(error, cause) {
}