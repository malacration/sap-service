package br.andrew.sap.model.sap

import br.andrew.sap.model.sap.documents.base.OrdemLinha
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Carregamento(
    val DocEntry: Int,
    val U_nameOrdem: String?,
    val U_docNumPedido: Int?,
    val U_Status: String?,
    val U_pesoTotal : Int,
    @JsonProperty("ORD_CRG_LINHACollection")
    var linhas: List<LinhaCarregamento>? = null
){}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class LinhaCarregamento {
    var DocEntry: Int? = null
    var LineId: Int? = null
    var U_orderDocEntry: Int? = null
    var U_docNumPedido: Int? = null
    var U_cardCode: String? = null
    var U_cardName: String? = null
    var U_quantidade: Int? = null
    var U_pesoItem: Int? = null
}

