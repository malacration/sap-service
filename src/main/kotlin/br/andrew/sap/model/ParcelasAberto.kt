package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ParcelasAberto(
    @JsonProperty("BPLName") val BPLName : String,
    val CardCode : String,
    val CardName : String,
    val SlpName : String,
    val Serial : String,
    val InstlmntID : String,
    val InsTotal : BigDecimal,
    val DueDate : String,
    val DocTotal : Double,
    val U_StatusCobranca: String?,
    val U_AgenteCobrador: String?,
    val PaidToDate : BigDecimal
){
    @get:JsonIgnore
    val saldo: BigDecimal
        get() = InsTotal.subtract(PaidToDate)

    fun getdueDateFormated() : String {
        return SimpleDateFormat("dd/MM/yyy").format(SimpleDateFormat("yyyyMMdd").parse(DueDate))
    }
}