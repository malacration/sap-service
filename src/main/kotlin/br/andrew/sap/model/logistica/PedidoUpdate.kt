package br.andrew.sap.model.logistica

import br.andrew.sap.services.batch.BatchId
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class PedidoUpdate(
    private val docEntry : String,
    val DocumentLines: List<PedidoUpdateLine>
) : BatchId {

    @JsonIgnore
    override fun getId(): String {
        return docEntry
    }
}


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class PedidoUpdateLine(val DocEntry : Int,
                       val LineNum : Int,
                       val U_ORD_CARREGAMENTO : Int?){

}