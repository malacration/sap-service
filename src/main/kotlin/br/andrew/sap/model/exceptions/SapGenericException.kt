package br.andrew.sap.model.exceptions

import br.andrew.sap.infrastructure.configurations.EventPublisherSingleton
import br.andrew.sap.model.SapError
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.base.Document

open class SapGenericException(val erro : SapError, val causa : Throwable? = null, extra : String = "") :
        Exception("Rntry: ${erro.entry} - "+erro.error.message.value+" - "+extra,causa)


