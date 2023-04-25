package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrderSales(CardCode: String,
                 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                 DocDueDate: String?,
                 DocumentLines: List<Product>,
                 BPL_IDAssignedToInvoice: String)
    : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice) {

    //TODO não achei onde fica esse propriedade
    var header : String? = null

    override fun toString(): String {
        return "OrderSales(CardCode='$CardCode', Branch='${getBPL_IDAssignedToInvoice()}', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }
}
//data de entrega - ORDRdocduedate
