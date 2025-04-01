package br.andrew.sap.model.sap.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

// br.andrew.sap.model.sap.partner/OrdemCarregamento.kt
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class OrdemCarregamento(
    @JsonProperty("U_orderDocEntry") val orderDocEntry: Int? = null,
    @JsonProperty("U_cardCode") val cardCode: String? = null,
    @JsonProperty("U_cardName") val cardName: String? = null,
    @JsonProperty("U_dataCriacao") val dataCriacao: Date? = null,
    @JsonProperty("U_valorFrete") val valorFrete: Double? = null,
    @JsonProperty("U_filial") val filial: Int? = null,
    @JsonProperty("U_vendedor") val vendedor: Double? = null,
    @JsonProperty("U_orderCode") val orderCode: String? = null,
    @JsonProperty("U_ordemName") val ordemName: String? = null,
    @JsonProperty("U_totalFrete") val totalFrete: Double? = null,
    @JsonProperty("U_pesoTotal") val pesoTotal: Double? = null,
    @JsonProperty("U_valorTotal") val valorTotal: Double? = null,
    @JsonProperty("U_codRegiao") val codRegiao: Int? = null,
    @JsonProperty("U_nomeRegiao") val nomeRegiao: String? = null,
    @JsonProperty("RV_ORD_LINHACollection") val linhas: List<OrdemCarregamentoLinha>? = null
)