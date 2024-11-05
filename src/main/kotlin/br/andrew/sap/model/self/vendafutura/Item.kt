package br.andrew.sap.model.self.vendafutura

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Item(
    val U_itemCode : String,
    val U_description : String,
    val U_precoNegociado : Double,
    var U_quantity : Double,
    val U_precoBase : Double,
    val U_desconto : Double,
    val U_comissao : Double,
    val U_MeasureUnit : String
){
    fun close() {
        LineStatus = "C"
    }

    fun total(): BigDecimal {
        return BigDecimal(U_precoNegociado.toString()).multiply(BigDecimal(U_quantity))
    }

    @JsonProperty("LineId")
    var LineId : Int? = null
    val VisOrder get() = LineId
    var LineStatus : String? = null

}