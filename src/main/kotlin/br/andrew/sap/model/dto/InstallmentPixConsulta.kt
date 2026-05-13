package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallmentPixConsulta(
    @JsonProperty("DocEntry")
    val docEntry: Int?,
    @JsonProperty("InstallmentId")
    val installmentId: Int?,
)
