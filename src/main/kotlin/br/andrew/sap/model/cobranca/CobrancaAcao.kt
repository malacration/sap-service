package br.andrew.sap.model.cobranca

class CobrancaAcaoRequest(
    val status: String? = null,
    val acao: String? = null,
    val situacao: String? = null,
    val ocorrencia: String? = null,
    val observacao: String? = null,
    val dataPromessa: String? = null,
)

class CobrancaAcaoLoteItem(
    val tipo: String,
    val docEntry: Int,
    val instlmntId: Int,
    val status: String? = null,
    val acao: String? = null,
    val situacao: String? = null,
    val ocorrencia: String? = null,
    val observacao: String? = null,
    val dataPromessa: String? = null,
) {
    fun toAcaoRequest() = CobrancaAcaoRequest(status, acao, situacao, ocorrencia, observacao, dataPromessa)
}

class CobrancaAcaoResultado(
    val tipo: String,
    val docEntry: Int,
    val instlmntId: Int,
    val success: Boolean,
    val error: String? = null,
)

class CobrancaException(message: String) : RuntimeException(message)
