package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class BusinessPartner(val cardCode : String) {

    @JsonIgnoreProperties
    private var BPPaymentMethods : List<PaymentMethod> = listOf()

    @JsonProperty("BPPaymentMethods")
    fun getBPPaymentMethods(): List<PaymentMethod> {
        return BPPaymentMethods
    }

    @JsonProperty("BPPaymentMethods")
    fun setBPPaymentMethods(valor : List<PaymentMethod>) {
        BPPaymentMethods = valor
    }
}