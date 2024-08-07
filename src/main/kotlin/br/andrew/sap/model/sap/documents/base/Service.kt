package br.andrew.sap.model.sap.documents.base

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
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