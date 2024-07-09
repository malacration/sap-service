package br.andrew.sap.model.documents.base

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Product(itemCode : String, quantity : String, unitPrice : String, usage : Int = 9)
    : DocumentLines(unitPrice, quantity, usage){

    init {
        this.ItemCode = itemCode
    }

    @JsonIgnore
    val id = itemCode

    override fun Duplicate(): Product {
        return Product(ItemCode!!,Quantity,UnitPrice,Usage).also {
            it.TaxCode = TaxCode
            it.AccountCode = AccountCode
        }
    }

    override fun toString(): String {
        return "$ItemCode - $ItemDescription - $Quantity"
    }
}