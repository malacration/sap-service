package br.andrew.sap.model.sap.documents.base

import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.uzzipay.Transaction
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.util.*

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Installment(
    @JsonProperty("DueDate")
    @JsonFormat(shape=JsonFormat.Shape.STRING, timezone="America/Porto_Velho")
    private val _dueDate : Date?, val total : Double) {

    var InstallmentId : Int? = null
    var PaymentOrdered : String? = null
    var Percentage : String? = null
    val TotalFC : Double? = null
    var U_QrCodePix : String? = null
    var U_pix_textContent : String? = null
    var U_pix_link : String? = null
    var U_pix_reference : String? = null

    var DocEntry : Int? = null



    val dueDate : String?
        get() = if(_dueDate == null) null else SimpleDateFormat("yyyy-MM-dd").format(_dueDate)

    fun createExternalIdentifier(document : Document): String {
        return  "Num${document.docNum}" +
                "-Entry${document.docEntry}" +
                "-ins:${this.InstallmentId}" +
                "-${document.docObjectCode}" +
                "-"+System.currentTimeMillis()
    }

    fun getBy(transaction: Transaction): Boolean {
        if(this.U_pix_reference == null)
            return false
        return (this.U_pix_reference == transaction.txId)
    }

    fun getBy(boleto: Boleto): Boolean {
        if(this.InstallmentId == null)
            return false
        return this.InstallmentId == boleto.numeroDaParcela
    }

    companion object{
        fun reverseExternalIdentifier(id : String) : String{
            return ""
        }
    }

}