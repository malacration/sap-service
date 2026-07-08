package br.andrew.sap.model.sysfeed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysfeedReceivingPending(
    val DocEntry: Int,
    val LineNum: Int,
    val DocNum: String? = null,
    val CardCode: String,
    val Serial: String? = null,
    val ItemCode: String? = null,
    val Quantity: String,
    val NrLoteCodigoRecebimento: String? = null,
    val DataValidade: String? = null,
    val DataFabricacao: String? = null,
    val DataRegistro: String? = null,
    val Placa: String? = null,
    val CodProd: String? = null,
    val SysfeedStatus: String? = null
)
