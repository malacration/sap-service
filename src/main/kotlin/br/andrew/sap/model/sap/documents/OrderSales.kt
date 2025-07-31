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
                 BPL_IDAssignedToInvoice: String = "")
    : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice) {

    var header : String? = null
    var DocEntry : String? = null
    var ItemCode : String? = null
    var Dscription : String? = null
    var BuyUnitMsr : String? = null
    var U_LocalidadeS : Number? = null
    var Name : String? = null
    var OnHand : String? = null
    var Quantity : Number? = null
    var Weight1 : Number? = null
    var IsCommited : Number? = null
    var UnitPrice : Number? = null
    var WarehouseCode : String? = null
    var Usage : Number? = null
    var TaxCode : String? = null
    var CostingCode : String? = null
    var CostingCode2 : String? = null
    var BaseLine : Number? = null
    var UomCode : String? = null
    var PrecoNegociado : Double? = null
    var PrecoBase : Double? = null
    var Comentario: String? = null
    var FretePorLinha: Double? = null

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

    fun toDocument(type : DocumentTypes): OrderSales {
        this.DocEntry = null
        this.DocumentLines.map { it.toInvoice(this.docObjectCode?.toString()?: throw Exception("O docType nao pode ser null")) }
        this.docObjectCode = type
        return this
    }
}
//data de entrega - ORDRdocduedate
