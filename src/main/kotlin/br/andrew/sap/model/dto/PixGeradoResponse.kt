package br.andrew.sap.model.dto

import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Installment
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PixGeradoResponse(
    val dueDate: String?,
    val total: Double,
    var status: DocumentStatus?,
    val installmentId: Int?,
    val paymentOrdered: String?,
    val percentage: String?,
    val totalFC: Double?,
    @JsonProperty("U_QrCodePix") val U_QrCodePix: String?,
    @JsonProperty("U_pix_textContent") val U_pix_textContent: String?,
    @JsonProperty("U_pix_link") val U_pix_link: String?,
    @JsonProperty("U_pix_reference") val U_pix_reference: String?,
    @JsonProperty("U_pix_due_date") val U_pix_due_date: String?,
    var docEntry: Int?,
    val taxaJurosMoraPercent: Double,
    val jurosValor: Double,
    val valorTitulo: Double,
    val valorTotal: Double,
    var docNum : String? = null
) {
    constructor(installment: Installment, taxaJurosMoraPercent: Double) : this(
        dueDate = installment.dueDate,
        total = installment.total,
        status = installment.Status,
        installmentId = installment.InstallmentId,
        paymentOrdered = installment.PaymentOrdered,
        percentage = installment.Percentage,
        totalFC = installment.TotalFC,
        U_QrCodePix = installment.U_QrCodePix,
        U_pix_textContent = installment.U_pix_textContent,
        U_pix_link = installment.U_pix_link,
        U_pix_reference = installment.U_pix_reference,
        U_pix_due_date = installment.U_pix_due_date,
        docEntry = installment.DocEntry,
        taxaJurosMoraPercent = taxaJurosMoraPercent,
        jurosValor = calcularJuros(installment, taxaJurosMoraPercent),
        valorTitulo = installment.total,
        valorTotal = installment.total + calcularJuros(installment, taxaJurosMoraPercent)
    )

    companion object {
        private fun calcularJuros(installment: Installment, taxaJurosMoraPercent: Double): Double {
            if (taxaJurosMoraPercent <= 0.0) {
                return 0.0
            }
            val dueDate = installment.dueDate ?: return 0.0
            val dueDateLocal = LocalDate.parse(dueDate)
            val now = LocalDate.now()
            val dataReferencia = if (dueDateLocal.isBefore(now)) now.plusDays(1) else now
            return installment.calcularJurosSimplesPorDia(taxaJurosMoraPercent, dataReferencia)
        }
    }
}
