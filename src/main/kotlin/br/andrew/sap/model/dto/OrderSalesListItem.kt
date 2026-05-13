package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderSalesListItem(
    val DocEntry: Int?,
    val DocNum: String?,
    val CardCode: String?,
    val CardName: String?,
    val ValorFrete: Double?,
    val DocTotal: Double?,
    val DocDate: String?,
    val DocumentStatus: String?,
    val SlpName: String?,
    val BPLName: String?,
    val BPLId: Int?,
    val DocObjectCode: String?
)
