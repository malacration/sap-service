package br.andrew.sap.model.sysfeed

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class SysfeedReceivingOrderRequest(
    val NrProducao: String,
    val CodProd: String,
    val CodFornecedor: String,
    val PesoNominalNF: String,
    val TipoProdutoRecebimento: String,
    val NrBag: String,
    val NrNotaFiscal: String? = null,
    val NrLoteCodigoRecebimento: String? = null,
    val Placa: String? = null,
    val RegLido: String = "NAO"
)
