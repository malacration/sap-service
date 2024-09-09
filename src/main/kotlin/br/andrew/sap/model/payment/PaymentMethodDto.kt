package br.andrew.sap.model.payment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class PaymentMethodDto(
    val payMethCod: String?,
    val description: String?,
    val branch: Int?,
    val dflAccount: String?,
    val glAccount: String?,
    @JsonProperty("BPLID")
    var BPLID: Int? = null
)