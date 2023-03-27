package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

abstract class Document(val CardCode : String,
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                    DocDueDate : Date,
                    val DocumentLines : List<Product>,
                    private val BPL_IDAssignedToInvoice : String,
                    val Usage : String) {

    val DocDueDate : String = "2023-03-20"

    @JsonProperty("BPL_IDAssignedToInvoice")
    fun getBPL_IDAssignedToInvoice(): String {
        return BPL_IDAssignedToInvoice;
    }

}

