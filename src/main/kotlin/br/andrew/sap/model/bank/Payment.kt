package br.andrew.sap.model.bank

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Payment() {
    constructor(transaction : Transaction, conta : ContaUzziPayPix, extraRemarks : String = "") : this(){
        //TODO adicionar conta contabil sap para conta uzzypay
        U_pix_reference = transaction.txId
        if(U_pix_reference == null)
            throw Exception("U_pix_reference não pode ser nulo")
        docDate = transaction.paymentDate
        cashSum = transaction.receivedAmount
        this.cashAccount = conta.contabil
        this.journalRemarks = "Pag. via Pix ${transaction.txId} $extraRemarks"
    }
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
    var journalRemarks : String? = null

    var U_pix_reference : String? = null

    @JsonProperty("BPLID")
    private var BPLID : String? = null

    @JsonProperty("BPLID")
    fun getBPLID() : String?{
        return BPLID
    }

    fun setBPID(BPLID : String?){
        this.BPLID = BPLID
    }
}






