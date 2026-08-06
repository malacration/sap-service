package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.time.LocalDate

// Faixas de aging. NAO saem de CASE WHEN no SQL (o parser do SQLQueries do SAP nao
// aceita) - cada faixa e uma janela de vencimento passada por parametro, e a conversao
// mora aqui pra regra existir num lugar so.
//
// DiasAtraso = hoje - DueDate, logo:
//   atraso >= diasMin  <=>  DueDate <= hoje - diasMin
//   atraso <= diasMax  <=>  DueDate >= hoje - diasMax
// E a mesma algebra que o diasAtrasoMin da consulta de titulos ja usa.
// A primeira faixa comeca em 1 dia, nao em 0: parcela que vence HOJE nao esta em atraso, e a
// tela de titulos ja usa diasAtrasoMin = 1 por padrao. Com 0 aqui, clicar na faixa "Até 30
// dias" abriria uma lista com MENOS linhas do que o card mostra - o drill-down entregaria a
// contradicao na cara do usuario.
enum class FaixaAtraso(val rotulo: String, val diasMin: Int, val diasMax: Int?) {
    ATE_30("Até 30 dias", 1, 30),
    DE_31_A_60("31 a 60 dias", 31, 60),
    DE_61_A_90("61 a 90 dias", 61, 90),
    ACIMA_DE_90("Mais de 90 dias", 91, null);

    fun vencimentoAte(hoje: LocalDate): LocalDate = hoje.minusDays(diasMin.toLong())

    fun vencimentoDe(hoje: LocalDate): LocalDate =
        diasMax?.let { hoje.minusDays(it.toLong()) } ?: LIMITE_INFERIOR

    companion object {
        // Sentinela larga em vez de omitir a comparacao: a view recebe sempre os dois
        // parametros, entao a faixa aberta precisa de um piso que nao corte nada.
        val LIMITE_INFERIOR: LocalDate = LocalDate.of(1900, 1, 1)
    }
}

// DiasMin/DiasMax viajam junto do numero de proposito: sao eles que o drill-down usa pra
// montar a janela de vencimento na tela de titulos. Se o front redefinisse as bordas por
// conta propria, as duas definicoes divergiriam em silencio e o card mostraria um total
// diferente da lista que ele abre.
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

// Uma linha por cobrador juntando esforco (TitulosTrabalhados) e resultado (Recuperado).
// Sem o esforco nao da pra separar "trabalhou e nao recuperou" de "nao trabalhou", que e
// exatamente o que a planilha nunca conseguiu responder.
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

// Resposta de GET /cobranca/dashboard. Carteira e sempre a foto de HOJE; Recuperado,
// TitulosTrabalhados e as promessas sao do periodo pedido (De..Ate) - por isso as duas
// datas voltam no corpo, pra tela poder rotular o card com o periodo certo em vez de
// assumir "mes corrente".
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaDashboard(
    val De: String,
    val Ate: String,
    // Janela anterior de MESMA duracao, terminando um dia antes de De. Volta no corpo pra o
    // card poder rotular "vs jul/26" em vez de um "vs anterior" que nao diz nada.
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
