package br.andrew.sap.model.sysfeed

data class SysfeedStatusUpdate(
    val tipo: SysfeedStatusTarget,
    val identificador: String,
    val status: String
)

data class SysfeedStatusUpdateResult(
    val tipo: SysfeedStatusTarget,
    val identificador: String,
    val status: String,
    val success: Boolean,
    val error: String? = null
)

enum class SysfeedStatusTarget {
    FORNECEDOR,
    ORDEM_RECEBIMENTO
}
