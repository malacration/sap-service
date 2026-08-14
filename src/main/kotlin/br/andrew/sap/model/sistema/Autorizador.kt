package br.andrew.sap.model.sistema

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

//roteamento motivo -> usuario (username da aplicacao, nao UserSign do SAP - a
//aprovacao acontece dentro do front-sap). independente de Autorizacao, mesmo
//espirito do LiberaPara da Comissao
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Autorizador(
    @JsonProperty("U_motivo") var U_motivo : String,
    @JsonProperty("U_usuario") var U_usuario : String,
) {
    var Code : Int? = null
    var Name : String? = null
}
