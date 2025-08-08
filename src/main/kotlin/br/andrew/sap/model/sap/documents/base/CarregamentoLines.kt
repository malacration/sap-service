package br.andrew.sap.model.sap.documents.base
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrdemLinha(
    val DocEntry: Int,
    val LineId: Int,
    val U_orderDocEntry: Int,
    val U_cardCode: String,
    val U_cardName: String,
    val U_docNumPedido: Int,
    val U_quantidade: Int
)
