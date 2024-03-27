package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.util.Date

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class ParcelasAberto(
    @JsonProperty("BPLName") val BPLName : String,
    val CardCode : String,
    val CardName : String,
    val SlpName : String,
    val Serial : String,
    val InstlmntID : String,
    val InsTotal : Double,
    val DueDate : String,
    val DocTotal : Double,
    val U_StatusCobranca: String?

){
    fun getdueDateFormated() : String {
        return SimpleDateFormat("dd/MM/yyy").format(SimpleDateFormat("yyyyMMdd").parse(DueDate))
    }


    fun notStatus() : String = if(U_StatusCobranca === null) "sem Status" else U_StatusCobranca




}