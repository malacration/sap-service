package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallmentPixResumo(
    @JsonProperty("DocEntry")
    val docEntry: Int?,
    @JsonProperty("DocNum")
    val docNum: String?,
    @JsonProperty("InstlmntID")
    val installmentId: Int?,
    @JsonProperty("U_QrCodePix")
    val qrCodePix: String?,
    @JsonProperty("ObjType")
    val objType: String?,
    @JsonProperty("Status")
    val status: String?,
    @JsonProperty("DueDate")
    val dueDate: String?,
    @JsonProperty("InsTotal")
    val insTotal: Double?,
    @JsonProperty("U_pix_textContent")
    val pixTextContent: String?,
    @JsonProperty("U_pix_link")
    val pixLink: String?,
    @JsonProperty("U_pix_reference")
    val pixReference: String?,
    @JsonProperty("U_pix_proxima_consulta_em")
    val pixProximaConsultaEm: String?,
    @JsonProperty("U_pix_consultar_ate")
    val pixConsultarAte: String?,
)
