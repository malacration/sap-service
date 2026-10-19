package br.andrew.sap.model.comercial
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class PrazoPagamentoDto(
    val GroupNum : String,
    //nulo na consulta do contrato quando a condicao (a vista, -1) nao tem linha no OCTG
    val PymntGroup : String?,
    val Code : String,
    val ListNum : String,
    val U_desconto : Double = 0.0,
    val U_juros : Double = 0.0){ }