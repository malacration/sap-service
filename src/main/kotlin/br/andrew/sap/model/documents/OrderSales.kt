package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class OrderSales(CardCode: String,
                 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                 DocDueDate: String?,
                 DocumentLines: List<Product>,
                 BPL_IDAssignedToInvoice: String)
    : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice) {




    //TODO não achei onde fica esse propriedade
    var header : String? = null
}


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class AdditionalExpenses(val expenseCode : Int, val lineTotalSys : Double){

    val distributionMethod : String = "aedm_RowTotal"
    val lineTotal : Double = lineTotalSys
    val LineGross : Double = lineTotalSys
    val LineGrossSys : Double = lineTotalSys
    companion object{
        @JsonIgnore
        fun frete(valor : Double) : AdditionalExpenses{
            return AdditionalExpenses(1,valor)
        }
    }
}
//data de entrega - ORDRdocduedate
