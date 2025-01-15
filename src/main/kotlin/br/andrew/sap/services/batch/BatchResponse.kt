package br.andrew.sap.services.batch

data class BatchResponse(
    val statusCode: Int,
    val body: String?,
    val success: Boolean,
    val errorCode: String? = null,
    val errorMessage: String? = null
)
