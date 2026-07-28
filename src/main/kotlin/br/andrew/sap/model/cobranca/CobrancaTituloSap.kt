package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Espelha 1:1 as colunas de cobranca-titulos.sql. O SQLQueries do SAP B1 usa um parser
// proprio (mais limitado que o HANA puro) que nao reconhece IFNULL/DAYS_BETWEEN/CASE
// WHEN nesse contexto - por isso os campos derivados (saldo, dias em atraso, situacao)
// sao calculados aqui em Kotlin, do mesmo jeito que ParcelasAberto/Installment ja fazem.
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaTituloSap(
    val DocEntry: Int,
    val DocNum: Int,
    val Serial: String?,
    val Series: Int?,
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
    val StatusParcela: String,
    val U_Status: String?,
    val U_Cobrador: String?,
    val U_Acao: String?,
    val U_Situacao: String?,
    val U_Ocorrencia: String?,
    val U_Observacao: String?,
    val U_DataAcao: String?,
    val U_DataPromessa: String?,
) {
    fun toDto(hoje: LocalDate = LocalDate.now()): CobrancaTitulo {
        val saldo = InsTotal.subtract(PaidToDate)
        val diasAtraso = ChronoUnit.DAYS.between(LocalDate.parse(DueDate, DateTimeFormatter.BASIC_ISO_DATE), hoje)
        // Situacao vem do proprio status da parcela no SAP ('O' aberta / 'C' fechada), nao do
        // saldo calculado (InsTotal - PaidToDate) - esse saldo pode dar negativo por rateio de
        // moeda/adiantamento vinculado mesmo com a parcela ainda aberta no SAP, o que classificava
        // titulo em aberto como "PAGO" errado.
        val situacaoSap = if (StatusParcela == "O") "ABERTO" else "PAGO"
        return CobrancaTitulo(
            Tipo = CobrancaRegistro.TIPO_NOTA_FISCAL,
            DocEntry = DocEntry,
            DocNum = DocNum,
            Serial = Serial,
            Series = Series,
            BPLId = BPLId,
            BPLName = BPLName,
            CardCode = CardCode,
            CardName = CardName,
            DocDate = DocDate,
            DocTotal = DocTotal,
            SlpCode = SlpCode,
            SlpName = SlpName,
            InstlmntID = InstlmntID,
            InsTotal = InsTotal,
            PaidToDate = PaidToDate,
            DueDate = DueDate,
            Saldo = saldo,
            DiasAtraso = diasAtraso,
            SituacaoSap = situacaoSap,
            U_Status = U_Status,
            U_Cobrador = U_Cobrador,
            U_Acao = U_Acao,
            U_Situacao = U_Situacao,
            U_Ocorrencia = U_Ocorrencia,
            U_Observacao = U_Observacao,
            U_DataAcao = U_DataAcao,
            U_DataPromessa = U_DataPromessa,
        )
    }
}
