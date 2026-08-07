package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.time.LocalDate

enum class FaixaAtraso(val rotulo: String, val diasMin: Int, val diasMax: Int?) {
    ATE_30("Até 30 dias", 1, 30),
    DE_31_A_60("31 a 60 dias", 31, 60),
    DE_61_A_90("61 a 90 dias", 61, 90),
    ACIMA_DE_90("Mais de 90 dias", 91, null);

    fun vencimentoAte(hoje: LocalDate): LocalDate = hoje.minusDays(diasMin.toLong())

    fun vencimentoDe(hoje: LocalDate): LocalDate =
        diasMax?.let { hoje.minusDays(it.toLong()) } ?: LIMITE_INFERIOR

    companion object {
        val LIMITE_INFERIOR: LocalDate = LocalDate.of(1900, 1, 1)
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaFaixa(
    val Faixa: String,
    val Saldo: BigDecimal,
    val Parcelas: Int,
    val DiasMin: Int,
    val DiasMax: Int?,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaPorFilial(
    @get:JsonProperty("BPLId") val BPLId: Int?,
    @get:JsonProperty("BPLName") val BPLName: String?,
    val Saldo: BigDecimal,
    val Parcelas: Int,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaPorStatus(
    val Status: String,
    val Saldo: BigDecimal,
    val Parcelas: Int,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaPorCobrador(
    val Cobrador: String,
    val Recuperado: BigDecimal,
    val Documentos: Int,
    val TitulosTrabalhados: Int,
    val ParcelasComPromessaVencida: Int,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaMes(
    val Mes: String,
    val Rotulo: String,
    val Recuperado: BigDecimal,
    val Documentos: Int,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaDashboard(
    val De: String,
    val Ate: String,
    val DeAnterior: String,
    val AteAnterior: String,
    val CarteiraSaldo: BigDecimal,
    val CarteiraParcelas: Int,
    val Recuperado: BigDecimal,
    val RecuperadoAnterior: BigDecimal,
    val RecuperadoDocumentos: Int,
    val SemAcaoSaldo: BigDecimal,
    val SemAcaoParcelas: Int,
    val PromessaVencidaSaldo: BigDecimal,
    val PromessaVencidaParcelas: Int,
    val Faixas: List<CobrancaFaixa>,
    val PorFilial: List<CobrancaPorFilial>,
    val PorStatus: List<CobrancaPorStatus>,
    val PorCobrador: List<CobrancaPorCobrador>,
)
