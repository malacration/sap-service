package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// Candidato a finalizacao vindo do para-finalizar.sql (retrieval, sem aritmetica).
// A comparacao (contrato.total() = produtos + frete vs apropriado) e feita no schedule.
@JsonIgnoreProperties(ignoreUnknown = true)
data class ContratoParaFinalizar(
    val docEntry: Int?,
    val apropriado: Double?,
)
