package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContasReceberDto (

    @JsonProperty("Ref1")
    val numeroPrimario: String?,

    @JsonProperty("CreatedBy")
    val docEntry: Int?,

    @JsonProperty("RefDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    val dataLancamento: LocalDate?,

    @JsonProperty("DueDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    val dataVencimento: LocalDate?,

    @JsonProperty("BPLName")
    val nomeFilial: String?,

    @JsonProperty("Debit")
    val debito: BigDecimal?,

    @JsonProperty("Credit")
    val credito: BigDecimal?,

    @JsonProperty("LineMemo")
    val observacao: String?,

    @JsonProperty("TransType")
    val tipoTransacao: Int?
) {
    val documento: String?
        get() = when (tipoTransacao) {
            13 -> "Nota Fiscal de Saída"
            14-> "Devs.Nota Fiscal de Saída"
            203 -> "Adiantamento de Cliente"
            else -> "Outro Documento"
        }
}
