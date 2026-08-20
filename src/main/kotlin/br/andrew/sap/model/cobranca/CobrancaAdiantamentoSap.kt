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

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaAdiantamentoSap(
    val DocEntry: Int,
    val DocNumAdiantamento: Int,
    val ContratoDocNum: Int?,
    @get:JsonProperty("BPLId") val BPLId: Int,
    @get:JsonProperty("BPLName") val BPLName: String?,
    val CardCode: String,
    val CardName: String,
    val Telefone: String?,
    val Celular: String?,
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
        val situacaoSap = if (StatusParcela == "O") "ABERTO" else "PAGO"
        return CobrancaTitulo(
            Tipo = CobrancaRegistro.TIPO_ADIANTAMENTO,
            DocEntry = DocEntry,
            DocNum = ContratoDocNum ?: DocNumAdiantamento,
            Serial = null,
            Series = null,
            BPLId = BPLId,
            BPLName = BPLName,
            CardCode = CardCode,
            CardName = CardName,
            Telefone = CobrancaTitulo.telefoneDeCobranca(Telefone, Celular),
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
