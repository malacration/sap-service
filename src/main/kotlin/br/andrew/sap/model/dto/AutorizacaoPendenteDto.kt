package br.andrew.sap.model.dto

//devolvido (HTTP 202) no lugar do documento quando o save cai numa regra de
//autorizacao - o front-sap usa isso pra diferenciar "criado" de "pendente"
data class AutorizacaoPendenteDto(
    val autorizacaoId: Int?,
    val motivo: String,
    val pendente: Boolean = true
)
