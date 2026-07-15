package br.andrew.sap.model.sysfeed

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysfeedStatusUpdate(
    val tipo: SysfeedStatusTarget,
    @JsonAlias("identificador")
    val codigo: String,
    val status: String,
    @JsonAlias("observacao", "erro", "mensagem", "message", "obsIntegracao", "Obs_Integracao")
    val obs: String? = null,
    @JsonAlias("nrSysfeed", "numero_sysfeed", "NumeroSysfeed")
    val numeroSysfeed: String? = null
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
