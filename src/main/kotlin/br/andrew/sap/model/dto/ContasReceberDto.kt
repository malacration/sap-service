package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.PixDocType
import br.andrew.sap.model.sap.documents.toPixDocTypeOrNull
import java.math.BigDecimal
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContasReceberDto (
    @JsonProperty("Ref1")
    val numeroPrimario: String?,
    @JsonProperty("CreatedBy")
    val CreatedBy: Int?,
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
    val tipoTransacao: Int?,
    @JsonProperty("SourceLine")
    val SourceLine : Int = 1
) {
    val documentType: DocumentTypes?
        get() = tipoTransacao?.let { DocumentTypes.fromValue(it) }

    val pixDocType: PixDocType?
        get() = documentType?.toPixDocTypeOrNull()

    val documento: String?
        get() = documentType?.label ?: "Outro Documento"
}
