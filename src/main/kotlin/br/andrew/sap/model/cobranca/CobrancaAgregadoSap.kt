package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaAgregadoSap(
    @get:JsonProperty("BPLId") val BPLId: Int?,
    @get:JsonProperty("BPLName") val BPLName: String?,
    val U_Status: String? = null,
    val U_Cobrador: String? = null,
    val Total: BigDecimal? = null,
    val Pago: BigDecimal? = null,
    val Parcelas: Int? = null,
) {
    fun saldo(): BigDecimal = (Total ?: BigDecimal.ZERO).subtract(Pago ?: BigDecimal.ZERO)
    fun parcelas(): Int = Parcelas ?: 0
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaRecuperadoSap(
    @get:JsonProperty("BPLId") val BPLId: Int?,
    @get:JsonProperty("BPLName") val BPLName: String?,
    val U_Cobrador: String? = null,
    val Recuperado: BigDecimal? = null,
    val Documentos: Int? = null,
) {
    fun recuperado(): BigDecimal = Recuperado ?: BigDecimal.ZERO
    fun documentos(): Int = Documentos ?: 0
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaTrabalhadosSap(
    val U_Usuario: String? = null,
    val Titulos: Int? = null,
) {
    fun titulos(): Int = Titulos ?: 0
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaRecuperadoDiaSap(
    val DocDate: String,
    val Recuperado: BigDecimal? = null,
    val DocEntry: Int,
) {
    fun recuperado(): BigDecimal = Recuperado ?: BigDecimal.ZERO
}
