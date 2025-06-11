package br.andrew.sap.model.sap

import br.andrew.sap.model.sap.documents.base.OrdemLinha
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Carregamento(
    val DocEntry: Int,
    val U_nameOrdem: String?,
    val U_docNumPedido: Int?,
    val U_Status: String?,
    val U_pesoTotal : Int,
    val ORD_CRG_LINHACollection: List<OrdemLinha> = emptyList()
){}


