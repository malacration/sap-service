package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.util.*

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Installment(@JsonProperty("DueDate") private val _dueDate : Date?, val total : Double) {

    var InstallmentId : Int? = null
    var PaymentOrdered : String? = null
    var Percentage : String? = null
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

    companion object{
        fun reverseExternalIdentifier(id : String) : String{
            return ""
        }
    }

}