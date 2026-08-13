package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaRegistro(
    var Code: String? = null,
    var U_Tipo: String? = null,
    var U_DocEntry: Int? = null,
    var U_InstlmntID: Int? = null,
    var U_CardCode: String? = null,
    var U_Status: String? = null,
    var U_Acao: String? = null,
    var U_Situacao: String? = null,
    var U_Ocorrencia: String? = null,
    var U_Observacao: String? = null,
    var U_Cobrador: String? = null,
    var U_DataAcao: String? = null,
    var U_DataPromessa: String? = null,
    @JsonProperty("COB_TITULO_LCollection")
    var historico: MutableList<CobrancaHistorico> = mutableListOf()
) {
    companion object {
        const val TIPO_NOTA_FISCAL = "NF"
        const val TIPO_ADIANTAMENTO = "AD"

        fun code(tipo: String, docEntry: Int, instlmntId: Int) = "$tipo-$docEntry-$instlmntId"
    }
}
