package br.andrew.sap.model.romaneio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class TipoAnalise(
        val U_CodTipoAnalise : String,
        val U_DscTipoAnalise : String,
        val U_ValorEncontrado : Double = 0.0,
        val U_DscUnidadeMedida : String?,
        val U_DescontoPeso : Double = 0.0
){
        val VisOrder = 1
        val Object = "PECU_UDO_REGR"


}