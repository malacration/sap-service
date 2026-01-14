package br.andrew.sap.model.logistica

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.Date

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Carregamento(
        val DocEntry: Int?,
        val U_nameOrdem: String?,
        var U_Status: String?,
        val CreateDate : String = Date().toInstant().toString(),
        val U_filial : Int?,
        val U_placa : String?,
        val U_motorista : String?,
        val U_capacidadeCaminhao : Double?,
        val U_transportadora : String?
){
        var Quantity : Double? = null
        var Weight1 : Double? = null
        var docEntryQuantity : Int? = null
}