package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
 class PainelIntegradoVendas(
    //val docEntry: Int,
    val DocDate: String?,
    val CardCode: String?,
    val CardName: String?,
    val SlpCode: String?,
    val SlpName: String?,
    val ItemCode: String,
    val Description: String?,
    val Usage: Int?,
    val DistribSum: BigDecimal?,
    val Quantity: BigDecimal?,
    val OnHand :BigDecimal,
)