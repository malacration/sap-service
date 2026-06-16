package br.andrew.sap.model.sysfeed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysfeedSupplierPending(
    val CardCode: String,
    val CardName: String,
    val Cnpj: String? = null,
    val InscricaoEstadual: String? = null,
    val Endereco: String? = null,
    val Cep: String? = null,
    val Telefone: String? = null,
    val SysfeedStatus: String? = null
)
