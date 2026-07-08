package br.andrew.sap.model.sysfeed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysfeedProductionOrderPending(
    val DocEntry: Long,
    val DocNum: String? = null,
    val ItemCode: String? = null,
    val Quantidade: String,
    val DataEntradaOP: String? = null,
    val DataEntregaProducao: String? = null,
    val DescricaoOrdemProducao: String? = null,
    val CodFormula: String? = null,
    val QuantBat: String? = null,
    val SysfeedStatus: String? = null
)
