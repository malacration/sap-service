package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

abstract class Document(val CardCode : String,
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                    val DocDueDate : String?,
                    val DocumentLines : List<Product>,
                    private val BPL_IDAssignedToInvoice : String,
                    val Usage : String?) {

    var DocEntry : String? = null
    var DocNum : String? = null

    @JsonProperty("BPL_IDAssignedToInvoice")
    fun getBPL_IDAssignedToInvoice(): String {
        return BPL_IDAssignedToInvoice;
    }

    fun productsByTax(): Map<String, List<Product>> {
        return this.DocumentLines
                .filter { it.TaxCode != null }
                .groupBy { it.TaxCode!! }
    }

}

