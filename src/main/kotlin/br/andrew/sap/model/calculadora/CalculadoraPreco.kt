package br.andrew.sap.model.calculadora

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CalculadoraPreco(
    @JsonProperty("U_descricao") val U_descricao : String,
    @JsonProperty("U_relatorioJson") val U_relatorioJson : String,
    @JsonProperty("U_lastUserId") var U_lastUserId : String? = null,
    @JsonProperty("U_lastUserOrigin") var U_lastUserOrigin: String? = null,
    @JsonProperty("U_lastUserName") var U_lastUserName: String? = null,
) {

}