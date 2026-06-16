package br.andrew.sap.model.sysfeed

data class SysfeedSupplierRequest(
    val cardCode: String,
    val cardName: String,
    val cnpj: String? = null,
    val inscricaoEstadual: String = "0",
    val endereco: String? = null,
    val cep: String? = null,
    val telefone: String? = null,
    val status: String = "ATIVO"
)
