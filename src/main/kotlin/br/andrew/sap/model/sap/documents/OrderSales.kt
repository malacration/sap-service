package br.andrew.sap.model.sap.documents

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class OrderSales(CardCode: String,
                 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                 DocDueDate: String?,
                 DocumentLines: List<DocumentLines>,
                 BPL_IDAssignedToInvoice: String = "2")
    : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice) {

    var header : String? = null
    var VehicleState : String? = null
    var BuyUnitMsr : String? = null
    var Weight1: Int? = null
    var ItemCode : String? = null
    var Dscription : String? = null
    var U_Localidade : Int? = null
    var Quantity: Double? = null
    var Name : String? = null

    override fun toString(): String {
        return "OrderSales(CardCode='$CardCode', Branch='${getBPL_IDAssignedToInvoice()}', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }


    fun duplicate() : OrderSales {
        val produtos = DocumentLines.map { it.Duplicate() }
        return OrderSales(CardCode,DocDueDate,produtos, getBPL_IDAssignedToInvoice()).also {
            it.model = model
            it.documentInstallments = this.documentInstallments
            it.journalMemo = this.journalMemo
            it.docDate = this.docDate
            it.controlAccount = this.controlAccount
        }
    }
}
//data de entrega - ORDRdocduedate
