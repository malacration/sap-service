package br.andrew.sap.model.sysfeed

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class SysfeedProductionOrderRequest(
    val codOrdemProducao: String,
    val codIntOrdemProducao: String,
    val codFormula: String,
    val codIntFormula: String,
    val quantidade: String,
    val prioridade: String,
    val totalQuantidade: String,
    val quantBat: String,
    val codAditivo: String? = null,
    val codLinha: String? = null,
    val dataEntradaOP: String? = null,
    val dataEntregaProducao: String? = null,
    val tipoOrdemProducao: String? = null,
    val descricaoOrdemProducao: String? = null,
    val codCliente: String? = null,
    val codGranja: String? = null,
    val nrOrdem: String? = null,
    val codLoteAnimal: String? = null,
    val codSiloDestino: String? = null
)
