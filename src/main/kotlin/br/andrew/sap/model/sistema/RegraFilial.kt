package br.andrew.sap.model.sistema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Em quais filiais cada regra do motor de autorizacao esta ativa (motivo -> filial).
 *
 * Uma linha por par, mesmo formato de Autorizador (motivo -> usuario). Cadastro
 * independente: nao e filha de Autorizacao nem de Autorizador.
 *
 * Motivo SEM nenhuma linha vale em TODA filial - ver RegraFilialService.ativaPara.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class RegraFilial(
    @JsonProperty("U_motivo") var U_motivo : String,
    //BPLId da filial, como texto: e assim que o Document carrega
    //(BPL_IDAssignedToInvoice) e evita comparar Int com String na hora de filtrar
    @JsonProperty("U_filial") var U_filial : String,
) {
    //Code do UDO e alfanumerico no service layer, nao numerico - mesma pegadinha
    //documentada em Comissao.kt e Autorizador.kt
    var Code : String? = null
    var Name : String? = null
}
