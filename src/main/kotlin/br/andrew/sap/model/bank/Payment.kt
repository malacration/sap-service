package br.andrew.sap.model.bank

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Payment {
    var cardCode : String? = null
    var cashAccount : String? = null
    var docDate : Date? = null
    var dueDate : Date? = null
    var cashSum : Double? = null
    var paymentInvoices : List<PaymentInvoice> = listOf()
    var docType : PaymentsTypeEnum? = null
    var BPLID : Int? = null
}


