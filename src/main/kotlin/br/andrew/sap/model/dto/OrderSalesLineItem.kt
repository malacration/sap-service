package br.andrew.sap.model.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderSalesLineItem(
    val ItemCode: String?,
    val Dscription: String?,
    val Quantity: Double?,
    val PrecoNegociado: Double?
)
