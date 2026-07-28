package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaHistorico(
    val U_Data: String,
    val U_Usuario: String,
    val U_Cobrador: String,
    val U_Status: String? = null,
    val U_Acao: String? = null,
    val U_Situacao: String? = null,
    val U_Ocorrencia: String? = null,
    val U_Observacao: String? = null,
    // Linha (bott_MasterDataLines) nao ganha CreateDate/CreateTime do SAP como a
    // master ganha - confirmado direto no banco. Por isso carimbamos a hora nós
    // mesmos nesse campo, em vez de depender de um campo de sistema.
    val U_Hora: String? = null,
) {
    var LineId: Int? = null
}
