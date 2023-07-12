package br.andrew.sap.model.bank

import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.PurchaseInvoice
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Payment {
    fun duplicateFor(newNota: Document): Payment {
        Payment().also {
            it.cardCode = this.cardCode
            it.cashAccount = this.cashAccount
            it.docDate = this.docDate
            it.dueDate = this.dueDate
            it.cashSum = this.cashSum
            it.docType = this.docType
            it.BPLID = this.BPLID
            it.paymentInvoices = this.paymentInvoices.map { it.duplicateFor(newNota) }
        }
        this.docNum = null
        return this
    }
    var docNum : Int? = null
    var docEntry: Int? = null
    var cardCode : String? = null
    var cashAccount : String? = null
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd")
    var docDate : String? = null
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd")
    var dueDate : String? = null
    var cashSum : Double? = null
    var paymentInvoices : List<PaymentInvoice> = listOf()
    var docType : PaymentsTypeEnum? = null
    var BPLID : Int? = null
}


