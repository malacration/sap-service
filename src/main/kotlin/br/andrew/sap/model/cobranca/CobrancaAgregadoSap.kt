package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

// Linha crua devolvida pelas views agregadas de carteira: cobranca-carteira.sql,
// cobranca-sem-acao.sql e cobranca-promessa-vencida.sql (e os pares -adiantamento).
// Uma classe so serve as tres porque as dimensoes que nao existem em cada view
// simplesmente nao vem no JSON e ficam nulas (@JsonIgnoreProperties + campo nulavel).
//
// Total/Pago vem separados porque o saldo NAO e subtraido no SQL: sem IFNULL (que o
// parser do SQLQueries nao aceita) um "PaidToDate" nulo envenenaria a soma inteira do
// grupo. A subtracao acontece no saldo() aqui embaixo.
//
// A contagem e de PARCELA (count(P."InstlmntID"), uma linha do join = uma parcela), nao
// de documento: a carteira e somada nas quatro faixas de aging, e uma nota com parcelas
// em faixas diferentes seria contada uma vez em cada se a chave fosse o DocEntry. Parcela
// tambem e a unidade que o time usa - as 213 linhas da planilha eram parcelas.
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

// Linha crua de cobranca-recuperado.sql / -adiantamento.sql. Aqui a contagem e por
// DOCUMENTO distinto de propósito: recuperado nao e quebrado em faixas, entao nao ha soma
// entre grupos pra duplicar, e "quantas notas voltaram" e o que o gestor pergunta.
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

// Linha crua de cobranca-trabalhados.sql / -adiantamento.sql. Conta TITULOS distintos
// tocados no periodo, nao acoes: cinco ligacoes no mesmo titulo nao e cinco vezes o
// trabalho de cinco titulos, e count(DISTINCT ...) e a unica forma de count com
// precedente nas views do projeto.
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaTrabalhadosSap(
    val U_Usuario: String? = null,
    val Titulos: Int? = null,
) {
    fun titulos(): Int = Titulos ?: 0
}

// Linha crua de cobranca-recuperado-diario.sql / -adiantamento.sql. Agrupa por data
// crua de pagamento (ORCT."DocDate") em vez de por mes porque nenhuma funcao de data
// tem precedente nas views do projeto - o agrupamento por mes e feito em Kotlin.
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaRecuperadoDiaSap(
    val DocDate: String,
    val Recuperado: BigDecimal? = null,
    val Documentos: Int? = null,
) {
    fun recuperado(): BigDecimal = Recuperado ?: BigDecimal.ZERO
    fun documentos(): Int = Documentos ?: 0
}
