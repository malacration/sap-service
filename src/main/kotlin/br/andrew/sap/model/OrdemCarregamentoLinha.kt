package br.andrew.sap.model.sap.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

// br.andrew.sap.model.sap.partner/OrdemCarregamentoLinha.kt
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class OrdemCarregamentoLinha(
    val DocEntry: Int? = null, // Adicionado
    @JsonProperty("U_docEntryPedido") val docEntryPedido: Int? = null,
    @JsonProperty("U_docNum") val docNum: Int? = null,
    @JsonProperty("U_codItemPedido") val codItemPedido: String? = null,
    @JsonProperty("U_nameItemPedido") val nameItemPedido: String? = null,
    @JsonProperty("U_codCliente") val codCliente: String? = null,
    @JsonProperty("U_nameCliente") val nameCliente: String? = null,
    @JsonProperty("U_quantidadePedido") val quantidadePedido: Double? = null,
    @JsonProperty("U_unidadeMedidaPedido") val unidadeMedidaPedido: String? = null,
    @JsonProperty("U_fretePedido") val fretePedido: Double? = null,
    @JsonProperty("U_precoNegociadoPedido") val precoNegociadoPedido: Double? = null,
    @JsonProperty("U_codRegiao") val codRegiao: Int? = null,
    @JsonProperty("U_nomeRegiao") val nomeRegiao: String? = null,
    @JsonProperty("U_pesoPedido") val pesoPedido: Double? = null,
    @JsonProperty("U_precoBase") val precoBase: Double? = null,
    @JsonProperty("U_desconto") val desconto: Double? = null,
    @JsonProperty("U_comissao") val comissao: Double? = null
)