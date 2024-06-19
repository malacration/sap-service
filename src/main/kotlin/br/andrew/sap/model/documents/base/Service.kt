package br.andrew.sap.model.documents.base

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Service(unitPrice : String, quantity : String = "0") : DocumentLines(unitPrice, quantity){

    override fun Duplicate(): Service {
        return Service(Quantity,UnitPrice).also {
            it.Usage = Usage
            it.TaxCode = TaxCode
            it.AccountCode = AccountCode
        }
    }
}