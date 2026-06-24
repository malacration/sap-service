package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Dados de PIX de uma parcela lidos direto do banco (via SQLQueries), usados para
 * confirmar a persistencia quando o GET do Service Layer retorna versao em cache.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallmentPixPersistido(
    @JsonProperty("InstallmentId")
    val installmentId: Int?,
    @JsonProperty("U_QrCodePix")
    val qrCodePix: String?,
    @JsonProperty("U_pix_reference")
    val pixReference: String?,
    @JsonProperty("U_pix_textContent")
    val pixTextContent: String?,
    @JsonProperty("U_pix_link")
    val pixLink: String?,
    @JsonProperty("U_pix_due_date")
    val pixDueDate: String?,
)
