package br.andrew.sap.model.sistema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

object TipoDocumentoAutorizacao {
    const val COTACAO = "COTACAO"
    const val PEDIDO_VENDA = "PEDIDO_VENDA"
}

object StatusAutorizacao {
    const val PENDENTE = "PENDENTE"
    const val APROVADO = "APROVADO"
    const val REJEITADO = "REJEITADO"
}

//um registro por documento retido pendente de autorizacao - U_payload guarda o
//documento original serializado, reenviado ao SAP no fluxo direto de sempre
//(QuotationsService.save/OrdersService.save) quando aprovado
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Autorizacao(
    @JsonProperty("U_tipoDocumento") var U_tipoDocumento : String,
    @JsonProperty("U_motivo") var U_motivo : String,
    @JsonProperty("U_cardCode") var U_cardCode : String,
    @JsonProperty("U_payload") var U_payload : String,
) {
    var Code : Int? = null
    var Name : String? = null

    @JsonProperty("U_cardName")
    var U_cardName : String? = null

    @JsonProperty("U_valor")
    var U_valor : Double? = null

    @JsonProperty("U_status")
    var U_status : String = StatusAutorizacao.PENDENTE

    @JsonProperty("U_solicitante")
    var U_solicitante : String? = null

    @JsonProperty("U_autorizador")
    var U_autorizador : String? = null

    @JsonProperty("U_observacao")
    var U_observacao : String? = null

    @JsonProperty("U_docEntryCriado")
    var U_docEntryCriado : Int? = null
}
