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
    var U_Status: String?,
    val U_pesoTotal2 : Int,
    val CreateDate : String?,
    val U_filial3 : Int?,
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
    var U_numDocPedido: Int? = null
    var U_cardCode: String? = null
    var U_cardName: String? = null
    var U_quantidade: Int? = null
    var U_pesoItem2: Int? = null
    var U_itemCode: String? = null
    var U_description: String? = null
    var U_precoUnitario: Int? = null
    var U_codigoDeposito: String? = null
    var U_usage: Int? = null
    var U_taxCode: String? = null
    var U_costingCode: String? = null
    var U_costingCode2: String? = null
    var U_baseType: Int? = null
    var U_baseEntry: Int? = null
    var U_baseLine: Int? = null
    var U_unMedida: String? = null
    var U_qtdEmEstoque : Number? = null
}

