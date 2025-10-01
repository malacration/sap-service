package br.andrew.sap.model.sap.documents

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.services.journal.EntryOriginalJournal
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.util.*
import br.andrew.sap.model.enums.Cancelled.tNO as tNO
import br.andrew.sap.model.enums.Cancelled.tYES as tYES


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Invoice(CardCode: String,
              @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
              DocDueDate: String?,
              DocumentLines: List<DocumentLines> = listOf(),
              BPL_IDAssignedToInvoice: String)
    : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice), EntryOriginalJournal {

    //TODO não achei onde fica esse propriedade
    var header : String? = null

    override fun toString(): String {
        return "OrderSales(CardCode='$CardCode', Branch='${getBPL_IDAssignedToInvoice()}', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }

    @JsonIgnore
    override fun getMemoForJournal() : String{
        return getDefaultForJournal(this,"Nota fiscal de Saída")
    }
}
