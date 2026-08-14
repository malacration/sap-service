package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

//resultado de cliente-em-atraso.sql - so a existencia de uma linha ja basta pra
//saber que o cliente tem titulo vencido e nao reconciliado
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TituloVencidoDto(
    @JsonProperty("TransId")
    val transId : Int?,
    @JsonProperty("ShortName")
    val cardCode : String?,
    @JsonProperty("DueDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    val dueDate : LocalDate?,
)
