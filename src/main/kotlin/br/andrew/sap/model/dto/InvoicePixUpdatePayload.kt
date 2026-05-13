package br.andrew.sap.model.dto

import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Installment
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class InvoicePixUpdatePayload(
    val DocumentInstallments: List<InstallmentPixUpdatePayload>
) {
    companion object {
        fun from(installments: List<Installment>): InvoicePixUpdatePayload {
            return InvoicePixUpdatePayload(
                DocumentInstallments = installments.map { InstallmentPixUpdatePayload.from(it) }
            )
        }
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class InstallmentPixUpdatePayload(
    @JsonProperty("InstallmentId")
    val InstallmentId : Int?,
    @JsonProperty("U_QrCodePix")
    val qrCodePix: String?,
    @JsonProperty("U_pix_textContent")
    val pixTextContent: String?,
    @JsonProperty("U_pix_link")
    val pixLink: String?,
    @JsonProperty("U_pix_reference")
    val pixReference: String?,
    @JsonProperty("U_pix_due_date")
    val pixDueDate: String?,
    @JsonProperty("U_pix_proxima_consulta_em")
    val pixProximaConsultaEm: String?,
    @JsonProperty("U_pix_consultar_ate")
    val pixConsultarAte: String?
) {

    companion object {
        fun from(installment: Installment): InstallmentPixUpdatePayload {
            return InstallmentPixUpdatePayload(
                InstallmentId = installment.InstallmentId,
                qrCodePix = installment.U_QrCodePix,
                pixTextContent = installment.U_pix_textContent,
                pixLink = installment.U_pix_link,
                pixReference = installment.U_pix_reference,
                pixDueDate = installment.U_pix_due_date,
                pixProximaConsultaEm = installment.U_pix_proxima_consulta_em,
                pixConsultarAte = installment.U_pix_consultar_ate
            )
        }
    }
}
