package br.andrew.sap.rovema.model.romaneio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class TipoAnalise(
        val U_CodTipoAnalise : String,
        val U_DscTipoAnalise : String,
        val U_ValorEncontrado : Double,
        val U_DscUnidadeMedida : String,
        val U_DescontoPeso : Double
){
        val VisOrder = 1
        val Object = "PECU_UDO_REGR"


}