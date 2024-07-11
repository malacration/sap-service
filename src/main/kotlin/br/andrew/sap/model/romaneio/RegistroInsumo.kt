package br.andrew.sap.model.romaneio

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class RegistroInsumo(
        val code : Int?,
        val U_CodigoFazenda : String?) {

    var U_CodParceiroNegocio : String? = null
    var U_NomParceiroNegocio : String? = null
    var U_CodigoFilial : String? = null
    var U_CodDeposito : String? = null
    var U_Status : String? = null
    var U_CodigoItem : String? = null
    var U_CodItem : String? = null
    var U_NomeItem : String? = null


}