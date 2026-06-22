package br.andrew.sap.model.sysfeed

import com.fasterxml.jackson.annotation.JsonAlias

data class SysfeedStatusUpdate(
    val tipo: SysfeedStatusTarget,
    @JsonAlias("identificador")
    val codigo: String,
    val status: String
)

data class SysfeedStatusUpdateResult(
    val tipo: SysfeedStatusTarget,
    val codigo: String,
    val status: String,
    val success: Boolean,
    val error: String? = null
)

enum class SysfeedStatusTarget {
    FORNECEDOR,
    ORDEM_RECEBIMENTO,
    ORDEM_PRODUCAO
}
