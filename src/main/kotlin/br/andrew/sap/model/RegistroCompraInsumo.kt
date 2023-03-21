package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class RegistroCompraInsumo(
        val code : Int?,
        val U_CodigoFazenda : String?) {

    var U_CodParceiroNegocio : String? = null
    var U_NomParceiroNegocio : String? = null
    var U_CodigoFilial : String? = null
    var U_CodDeposito : String? = null


}