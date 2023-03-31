package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonFormat
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
                 BPL_IDAssignedToInvoice: String,
                 Usage: String?) : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice, Usage) {




    //TODO não achei onde fica esse propriedade
    var Header : String? = null
}


//obs da nota - oinv Header
//observação pedido - ORDR.comments
//data de entrega - ORDRdocduedate
//desconto do vendedor(linha) - RDRdiscPrcnt
//desconto da contdição(geral) - ordr, discPricnt
//Forma de pagamento - ORDR.peymethod
//Grupo economico RDR1."CogsOcrCod"
//Centro de custo RDR1CogsOcrCo2
//Utilização - RDR1.usage