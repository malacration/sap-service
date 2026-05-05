package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PedidoTesteRequest(
    val quantidade: Int,
    val localidade: String,
    val filial: Int? = null,
    val dataInicial: String? = null,
    val dataFinal: String? = null
)
