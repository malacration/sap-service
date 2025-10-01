package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate

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
    val valor: BigDecimal?,

    @JsonProperty("LineMemo")
    val observacao: String?,

    @JsonProperty("TransType")
    val tipoTransacao: Int?
) {
    val documento: String?
        get() = when (tipoTransacao) {
            13 -> "Nota Fiscal de Saída"
            203 -> "Adiantamento de Cliente"
            else -> "Outro Documento"
        }
}
