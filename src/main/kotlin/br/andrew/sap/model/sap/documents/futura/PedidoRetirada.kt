package br.andrew.sap.model.sap.documents.futura

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class PedidoRetirada(
    val docEntryVendaFutura : Int,
    val itensRetirada : List<ItemRetirada>){

}


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ItemRetirada(
    val itemCode: String,
    val quantity: Double){

}