package br.andrew.sap.model.sap.documents

import br.andrew.sap.model.sap.ReconciliationRow
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.services.batch.BatchId
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class DownPayment(CardCode: String,
                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                  DocDueDate: String?,
                  DocumentLines: List<DocumentLines> = listOf(),
                  BPL_IDAssignedToInvoice: String)
    : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice) {

    //TODO não achei onde fica esse propriedade
    var header : String? = null
    val DownPaymentType = "dptInvoice"

    @JsonIgnore
    var apropriado: BigDecimal = BigDecimal.ZERO

    fun adiantamentoDisponivel() : BigDecimal{
        return BigDecimal(this.DocTotal)-apropriado
    }
    override fun toString(): String {
        return "DownPayment(CardCode='$CardCode', Branch='${getBPL_IDAssignedToInvoice()}', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }
}
//data de entrega - ORDRdocduedate


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class DownPaymentUnsetVendaFutura(private val _id : String) : BatchId{
    constructor(document: Document) : this(document.docEntry.toString())

    val U_venda_futura : String? = null
    override fun getId(): String {
        return _id
    }
}
