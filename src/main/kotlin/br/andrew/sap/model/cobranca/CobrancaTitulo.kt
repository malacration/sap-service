package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

// DTO de resposta ao front: os campos calculados (Saldo, DiasAtraso, SituacaoSap) ja
// vem prontos daqui, calculados em Kotlin por CobrancaTituloSap.toDto() - a SQLQueries
// do SAP so devolve as colunas cruas.
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaTitulo(
    val Tipo: String,
    val DocEntry: Int,
    val DocNum: Int,
    val Serial: String?,
    val Series: Int?,
    // Sem @get:JsonProperty explícito, a serialização de saída (o JSON que vai pro front)
    // vira "Bplid"/"Bplname" (o UpperCamelCaseStrategy mexe em nomes com 3+ maiúsculas
    // seguidas no início) e o front, que espera "BPLId"/"BPLName", nunca acha o valor.
    // "@JsonProperty" sem alvo explícito na property de um construtor Kotlin vai pro
    // parâmetro (usado na leitura) e não pro getter (usado na escrita) - por isso o
    // "@get:" é obrigatório aqui, não é só estilo.
    @get:JsonProperty("BPLId") val BPLId: Int,
    @get:JsonProperty("BPLName") val BPLName: String?,
    val CardCode: String,
    val CardName: String,
    val DocDate: String?,
    val DocTotal: BigDecimal,
    val SlpCode: Int?,
    val SlpName: String?,
    val InstlmntID: Int,
    val InsTotal: BigDecimal,
    val PaidToDate: BigDecimal,
    val DueDate: String,
    val Saldo: BigDecimal,
    val DiasAtraso: Long,
    val SituacaoSap: String,
    val U_Status: String?,
    val U_Cobrador: String?,
    val U_Acao: String?,
    val U_Situacao: String?,
    val U_Ocorrencia: String?,
    val U_Observacao: String?,
    val U_DataAcao: String?,
    val U_DataPromessa: String?,
) {
    val code: String
        get() = CobrancaRegistro.code(Tipo, DocEntry, InstlmntID)
}
